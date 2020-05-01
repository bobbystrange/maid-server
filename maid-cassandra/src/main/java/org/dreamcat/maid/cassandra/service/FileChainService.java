package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileChainService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final CassandraTemplate cassandraTemplate;

    public void associate(UserFileEntity directory, UserFileEntity file) {
        // only null as a empty directory and never add a item to it before
        Set<String> items = directory.getItems();
        if (ObjectUtil.isEmpty(items)) items = new HashSet<>();

        items.add(FileUtil.basename(file.getPath()));
        directory.setItems(items);
    }

    // /a/b/c -> [/, /a, /a/b, /a/b/c]
    public List<String> splitPath(String path) {
        return Arrays.stream(path.split("/")).reduce(new ArrayList<>(), (pv, cv) -> {
            if (cv.isEmpty()) {
                pv.add("/");
            } else {
                String parentPath = pv.get(pv.size() - 1);
                if (parentPath.endsWith("/")) {
                    pv.add(parentPath + cv);
                } else {
                    pv.add(parentPath + "/" + cv);
                }
            }
            return pv;
        }, (left, right) -> {
            left.addAll(right);
            return left;
        });
    }

    // Fixme concurrent invocation issues
    public void mkdir(String path, UUID uid) {
        long timestamp = System.currentTimeMillis();
        Map<String, UserFileEntity> directories = retrospectMkdirs(uid, path, timestamp);
        if (ObjectUtil.isNotEmpty(directories)) {
            cassandraTemplate.batchOps()
                    .insert(directories.values()).execute();
        }
    }

    // /a/b/c/d, just exist /a/b, then [/a/b, /a/b/c, /a/b/c/d]
    public Map<String, UserFileEntity> retrospectMkdirs(UUID uid, String path, long timestamp) {
        List<String> paths = splitPath(path);
        Map<String, UserFileEntity> entities = userFileDao.findAll(uid, paths).stream()
                .collect(Collectors.toMap(UserFileEntity::getPath, it -> it));
        Map<String, UserFileEntity> updatedEntities = new HashMap<>();

        for (String i : paths) {
            UserFileEntity file = entities.get(i);
            if (file != null) continue;

            // has i but has not i - 1
            file = new UserFileEntity();
            commonService.fillEntity(file, timestamp, uid, i);
            // never null
            String iMinusOne = FileUtil.dirname(i);
            UserFileEntity directory = entities.get(iMinusOne);
            directory.setMtime(timestamp);
            associate(directory, file);
            entities.put(i, file);
            updatedEntities.putIfAbsent(iMinusOne, directory);
            updatedEntities.put(i, file);
        }
        return updatedEntities;
    }

    public void moveOrCopy(String fromPath, String toPath, ServerWebExchange exchange, boolean move) {
        commonService.checkPaths(fromPath, toPath);
        if (toPath.startsWith(fromPath)) {
            throw new ForbiddenException("Target path " + toPath + "is the sub entry of source path " + fromPath);
        }

        var uid = commonService.retrieveUid(exchange);
        var fromFile = userFileDao.find(uid, fromPath);
        var toDir = userFileDao.find(uid, toPath);

        if (fromFile == null) {
            throw new NotFoundException("Source path " + fromPath + " doesn't exist");
        }
        if (commonService.isDirectory(fromFile)) {
            if (toDir == null) {
                throw new NotFoundException("Target path " + toPath + " doesn't exist");
            }
            if (commonService.isFile(toDir)) {
                throw new ForbiddenException("Target directory " + toPath + " is a file");
            }
            moveOrCopyDirectory(fromFile, toDir, uid, move);
            return;
        }

        if (toDir != null && commonService.isFile(toDir)) {
            throw new ForbiddenException("Target directory " + toPath + " is a file");
        }

        var timestamp = System.currentTimeMillis();
        var oldFromFile = BeanCopierUtil.copy(fromFile, UserFileEntity.class);
        var ops = cassandraTemplate.batchOps();

        // unlink from-file from from-directory
        if (move) {
            var fromDirPath = FileUtil.dirname(fromPath);
            var fromDirectory = userFileDao.find(uid, fromDirPath);
            fromDirectory.getItems().remove(FileUtil.basename(fromFile.getPath()));
            ops.insert(fromDirectory);
        }

        // create new directories for to-directory
        if (toDir == null) {
            Map<String, UserFileEntity> directories = retrospectMkdirs(uid, toPath, timestamp);
            toDir = directories.remove(toPath);
            if (!directories.isEmpty()) {
                ops.insert(directories.values());
            }
        }

        // link from-file to to-directory
        var basename = FileUtil.basename(fromPath);
        var toFilePath = FileUtil.normalize(toPath + "/" + basename);
        fromFile.setPath(toFilePath);
        fromFile.setMtime(timestamp);
        associate(toDir, fromFile);
        if (move) {
            ops.delete(oldFromFile);
        }
        ops.insert(fromFile).insert(toDir).execute();
    }

    private void moveOrCopyDirectory(UserFileEntity fromFile, UserFileEntity toDir, UUID uid, boolean move) {
        var fromPath = fromFile.getPath();
        var toPath = toDir.getPath();
        long timestamp = System.currentTimeMillis();
        var oldFromFile = BeanCopierUtil.copy(fromFile, UserFileEntity.class);
        var ops = cassandraTemplate.batchOps();
        var opsCounter = new AtomicInteger((move ? 1 << 11 : 1 << 10) - 2);

        // unlink from-file from from-directory
        if (move) {
            var fromDirPath = FileUtil.dirname(fromPath);
            var fromDir = userFileDao.find(uid, fromDirPath);
            fromDir.getItems().remove(FileUtil.basename(fromPath));
            ops.insert(fromDir);
            opsCounter.decrementAndGet();
        }

        // link from-file to to-directory
        var basename = FileUtil.basename(fromPath);
        var toFilePath = FileUtil.normalize(toPath + "/" + basename);
        fromFile.setPath(toFilePath);
        fromFile.setMtime(timestamp);
        associate(toDir, fromFile);
        if (move) {
            ops.delete(oldFromFile);
            opsCounter.decrementAndGet();
        }

        ops.insert(toDir).insert(fromFile);
        var items = userFileDao.findAllItems(fromFile);
        for (UserFileEntity item : items) {
            recurseMoveOrCopyItem(item, fromFile, toDir, basename, ops, opsCounter, move);
        }
        ops.execute();
    }

    // move /a/b/c/d to /a/b/x/y  ->  /a/b/x/y/d
    // then rename [/a/b/c/d] [/k1/k2/k3.k4]  to  [/a/b/x/y] [/d] /k1/k2/k3.k4
    private void recurseMoveOrCopyItem(
            UserFileEntity item, UserFileEntity fromDir,
            UserFileEntity toDir, String basename,
            CassandraBatchOperations ops, AtomicInteger opsCounter, boolean move) {
        if (opsCounter.get() < 0) {
            throw new ForbiddenException("Too many entities (expect less than or equals 1024)");
        }
        if (move) {
            ops.delete(BeanCopierUtil.copy(item, UserFileEntity.class));
            opsCounter.decrementAndGet();
        }

        // /a/b/c/d/k1/k2/k3.k4
        String path = item.getPath();
        // /k1/k2/k3.k4
        path = path.replace(fromDir.getPath(), "");
        // /a/b/x/y  /d  /k1/k2/k3.k4
        path = toDir.getPath() + "/" + basename + path;
        item.setPath(path);

        ops.insert(item);
        opsCounter.decrementAndGet();

        if (commonService.isDirectory(item)) {
            List<UserFileEntity> items = userFileDao.findAllItems(item);
            for (UserFileEntity i : items) {
                recurseMoveOrCopyItem(i, fromDir, toDir, basename, ops, opsCounter, move);
            }
        }
    }

}
