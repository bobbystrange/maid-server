package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
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
    public Map<String, UserFileEntity> retrospectCreateDirectories(UUID uid, String path, long timestamp) {
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

    public void recurseList(FileView tree, UUID uid, int level) {
        if (level <= 0) return;
        String path = tree.getPath();
        UserFileEntity directory = userFileDao.find(uid, path);
        if (directory == null) return;
        if (commonService.isFile(directory)) return;

        Set<String> items = directory.getItems();
        if (ObjectUtil.isEmpty(items)) return;

        List<FileView> views = userFileDao.findAll(uid, items, path).stream()
                .map(it -> {
                    FileView view = BeanCopierUtil.copy(it, FileView.class);
                    view.setName(FileUtil.basename(view.getPath()));
                    if (commonService.isDirectory(it)) {
                        view.setItems(new ArrayList<>());
                    }
                    return view;
                }).collect(Collectors.toList());

        tree.setItems(views);

        for (FileView it : views) {
            if (it.getItems() == null) continue;
            recurseList(it, uid, level - 1);
        }
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

    public void rename(UserFileEntity entity, String newName) {
        var path = entity.getPath();
        if ("/".equals(path)) {
            log.warn("Unsupported to rename root directory / ");
            return;
        }
        var parentPath = FileUtil.dirname(path);
        var entities = new ArrayList<UserFileEntity>();
        var timestamp = System.currentTimeMillis();

        entity.setPath(FileUtil.normalize(parentPath + "/" + newName));
        recurseRename(parentPath, entity, entities, timestamp);
        userFileDao.saveAll(entities);
    }

    private void recurseRename(String parentPath, UserFileEntity entity, List<UserFileEntity> entities, long timestamp) {
        // avoid oom
        if (entities.size() > 1 << 10) {
            throw new ForbiddenException("too many entities, more than 1024");
        }

        entity.setPath(FileUtil.normalize(parentPath + "/" + FileUtil.basename(entity.getPath())));
        entity.setMtime(timestamp);
        entities.add(entity);

        Set<String> items = entity.getItems();
        if (ObjectUtil.isEmpty(items)) return;

        List<UserFileEntity> subEntities = userFileDao.findAllItems(entity);
        for (UserFileEntity subEntity : subEntities) {
            recurseRename(entity.getPath(), subEntity, entities, timestamp);
        }
    }

    public void delete(UserFileEntity entity, UUID uid) {
        var ops = cassandraTemplate.batchOps();

        var path = entity.getPath();
        // need update parent dir's items;
        if (!path.equals("/")) {
            var basename = FileUtil.basename(path);
            var dirname = FileUtil.dirname(path);
            var dir = userFileDao.find(uid, dirname);
            var items = dir.getItems();
            items.remove(basename);
            dir.setItems(items);
            ops.update(dir);
        }

        if (commonService.isFile(entity)) {
            ops.delete(entity).execute();
            return;
        }

        List<UserFileEntity> entities = new ArrayList<>();
        recurseDelete(entity, entities);
        ops.delete(entities).execute();
    }

    private void recurseDelete(UserFileEntity entity, List<UserFileEntity> entities) {
        // avoid oom
        if (entities.size() > 1 << 10) {
            throw new ForbiddenException("too many entities, more than 1024");
        }
        entities.add(entity);
        if (commonService.isDirectory(entity)) {
            List<UserFileEntity> items = userFileDao.findAllItems(entity);
            for (UserFileEntity item : items) {
                recurseDelete(item, entities);
            }
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
