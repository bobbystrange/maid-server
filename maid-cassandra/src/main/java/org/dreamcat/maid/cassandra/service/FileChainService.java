package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

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

    // move /a/b/c/d to /a/b/x/y  ->  /a/b/x/y/d
    // then rename [/a/b/c/d] [/k1/k2/k3.k4]  to  [/a/b/x/y] [/d] /k1/k2/k3.k4
    public void recurseMoveItem(
            UserFileEntity item, UserFileEntity fromDir,
            UserFileEntity toDir, String basename,
            List<UserFileEntity> entities, List<UserFileEntity> oldEntities) {
        // avoid oom
        if (entities.size() > 1 << 10) {
            throw new ForbiddenException("too many entities, more than 1024");
        }

        if (oldEntities != null) oldEntities.add(BeanCopierUtil.copy(item, UserFileEntity.class));

        // /a/b/c/d/k1/k2/k3.k4
        String path = item.getPath();
        // /k1/k2/k3.k4
        path = path.replace(fromDir.getPath(), "");
        // /a/b/x/y  /d  /k1/k2/k3.k4
        path = toDir.getPath() + "/" + basename + path;
        item.setPath(path);

        entities.add(item);
        if (commonService.isDirectory(item)) {
            List<UserFileEntity> items = userFileDao.findAllItems(item);
            for (UserFileEntity i : items) {
                recurseMoveItem(i, fromDir, toDir, basename, entities, oldEntities);
            }
        }
    }

    public void rename(UserFileEntity entity, UUID uid, String newName) {
        var path = entity.getPath();
        if ("/".equals(path)) {
            log.warn("Unsupported to rename root directory / ");
            return;
        }

        var parentPath = FileUtil.dirname(path);
        var newPath = FileUtil.normalize(parentPath + "/" + newName);
        if (userFileDao.find(uid, newPath) != null) {
            throw new ForbiddenException("file already exists");
        }

        var timestamp = System.currentTimeMillis();
        var ops = cassandraTemplate.batchOps();
        var opsCounter = new AtomicInteger(1 << 10 - 3);

        var parentDir = userFileDao.find(uid, parentPath);
        var oldName = FileUtil.basename(path);
        parentDir.getItems().remove(oldName);
        parentDir.getItems().add(newName);
        ops.update(parentDir);

        var oldEntity = new UserFileEntity();
        oldEntity.setPath(path);
        oldEntity.setUid(uid);
        ops.delete(oldEntity);

        entity.setPath(newPath);
        entity.setMtime(timestamp);
        ops.insert(entity);

        if (ObjectUtil.isNotEmpty(entity.getItems())) {
            var entities = userFileDao.findAllItems(entity);
            for (var e : entities) {
                recurseRename(entity, e, ops, opsCounter);
            }
        }
        ops.execute();
    }

    private void recurseRename(UserFileEntity parent, UserFileEntity entity, CassandraBatchOperations ops, AtomicInteger opsCounter) {
        // avoid oom
        if (opsCounter.get() < 0) {
            throw new ForbiddenException("too many entities, more than 1024");
        }

        var oldEntity = new UserFileEntity();
        oldEntity.setPath(entity.getPath());
        oldEntity.setUid(entity.getUid());
        ops.delete(oldEntity);
        opsCounter.getAndDecrement();

        entity.setPath(FileUtil.normalize(parent.getPath() + "/" + FileUtil.basename(entity.getPath())));
        entity.setMtime(parent.getMtime());
        ops.insert(entity);
        opsCounter.getAndDecrement();

        var items = entity.getItems();
        if (ObjectUtil.isEmpty(items)) return;

        var entities = userFileDao.findAllItems(entity);
        for (var e : entities) {
            recurseRename(entity, e, ops, opsCounter);
        }
    }

    public void updateFileContent(UserFileEntity entity, String signature, String type) {
        long timestamp = System.currentTimeMillis();
        entity.setMtime(timestamp);
        entity.setDigest(signature);
        entity.setType(type);

        FileEntity realFile = new FileEntity();
        realFile.setDigest(signature);
        realFile.setType(type);

        // keep consistency
        cassandraTemplate.batchOps()
                .insert(entity)
                .insert(realFile)
                .execute();
    }

    public void copy(
            UserFileEntity fromDir,
            UserFileEntity toDir) {
        var fromPath = fromDir.getPath();
        var toPath = toDir.getPath();
        var path = FileUtil.normalize(toPath + "/" + FileUtil.basename(fromPath));
        fromDir.setPath(path);

    }
}
