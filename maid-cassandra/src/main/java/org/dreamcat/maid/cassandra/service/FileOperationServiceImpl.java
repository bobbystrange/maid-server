package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.service.FileOperationService;
import org.dreamcat.maid.api.util.TikaUtil;
import org.dreamcat.maid.cassandra.dao.FileDao;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/23
 */
@RequiredArgsConstructor
@Service
public class FileOperationServiceImpl implements FileOperationService {
    private final UserFileDao userFileDao;
    private final FileDao fileDao;
    private final CommonService commonService;
    private final FileChainService fileChainService;
    private final CassandraTemplate cassandraTemplate;

    @Override
    public RestBody<String> catFile(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity file = userFileDao.find(uid, path);
        if (file == null) {
            return RestBody.error("File %s doesn't exist", path);
        }
        if (commonService.isDirectory(file)) {
            return RestBody.error("%s is not a file", path);
        }

        String signurate = file.getDigest();
        String type = file.getType();
        FileEntity realFile = fileDao.findById(signurate).orElseThrow(() ->
                new NotFoundException("File " + path + " is not found"));

        String content = null;
        if (TikaUtil.isBinary(type)) {
            return RestBody.error("Binary file %s cannot be read as string");
        }

        // todo read from file system
        return RestBody.ok(content);
    }

    @Override
    public RestBody<?> createDirectory(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        path = FileUtil.normalize(path);
        long timestamp = System.currentTimeMillis();
        Map<String, UserFileEntity> directories = fileChainService.retrospectCreateDirectories(uid, path, timestamp);
        if (ObjectUtil.isNotEmpty(directories)) {
            cassandraTemplate.batchOps().insert(directories.values()).execute();
        }
        return RestBody.ok();
    }

    @Override
    public RestBody<FileView> listDirectory(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity directory = userFileDao.find(uid, path);
        if (directory == null) {
            return RestBody.error("Directory %s doesn't exist", path);
        }
        if (commonService.isFile(directory)) {
            return RestBody.error("%s is not a diretory", path);
        }

        FileView directoryView = BeanCopierUtil.copy(directory, FileView.class);
        directoryView.setName(FileUtil.basename(directoryView.getPath()));
        fileChainService.recurseList(directoryView, uid, 1);
        return RestBody.ok(directoryView);
    }

    @Override
    public RestBody<FileView> listDirectoryTree(String path, ServerWebExchange exchange) {
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity directory = userFileDao.find(uid, path);
        if (directory == null) {
            return RestBody.error("Directory %s doesn't exist", path);
        }
        if (commonService.isFile(directory)) {
            return RestBody.error("%s is not a diretory", path);
        }

        FileView directoryView = BeanCopierUtil.copy(directory, FileView.class);
        directoryView.setName(FileUtil.basename(directoryView.getPath()));
        fileChainService.recurseList(directoryView, uid, 127);
        return RestBody.ok(directoryView);
    }

    @Override
    public RestBody<Map<String, List<FileItemView>>> getPathMap(ServerWebExchange exchange) {
        var result = listDirectoryTree("/", exchange);
        if (result.getCode() != 0) {
            return RestBody.error(result.getMsg());
        }

        var view = result.getData();
        var map = new HashMap<String, List<FileItemView>>();
        recurseFillPathMap(view, map);
        return RestBody.ok(map);
    }

    private void recurseFillPathMap(FileView view, Map<String, List<FileItemView>> map) {
        var path = view.getPath();
        var items = view.getItems();
        if (ObjectUtil.isEmpty(items)) {
            map.put(path, new ArrayList<>());
            return;
        }

        map.put(path, items.stream()
                .map(it -> {
                    var itemView = BeanCopierUtil.copy(it, FileItemView.class);
                    if (itemView.getDigest() == null) {
                        var v = it.getItems();
                        itemView.setCount(v != null ? v.size() : 0);
                    }
                    return itemView;
                })
                .collect(Collectors.toList()));

        for (var i : items) {
            recurseFillPathMap(i, map);
        }
    }

    @Override
    public RestBody<?> moveFile(String fromPath, String toPath, ServerWebExchange exchange) {
        fromPath = FileUtil.normalize(fromPath);
        toPath = FileUtil.normalize(toPath);
        commonService.checkPath(fromPath, toPath);
        if (toPath.startsWith(fromPath)) {
            throw new ForbiddenException("Target path " + toPath + "is the sub entry of source path " + fromPath);
        }

        var uid = commonService.retrieveUid(exchange);
        var fromFile = userFileDao.find(uid, fromPath);
        var toDir = userFileDao.find(uid, toPath);

        if (fromFile == null) {
            return RestBody.error("Source path %s doesn't exist", fromPath);
        }
        if (commonService.isDirectory(fromFile)) {
            if (toDir == null) {
                return RestBody.error("Target path %s doesn't exist", toPath);
            }
            if (commonService.isFile(toDir)) {
                throw new ForbiddenException("Target directory " + toPath + " is a file");
            }
            return moveDirectory(fromFile, toDir, uid);
        }

        if (toDir != null && commonService.isFile(toDir)) {
            throw new ForbiddenException("Target directory " + toPath + " is a file");
        }

        long timestamp = System.currentTimeMillis();
        var oldFromFile = BeanCopierUtil.copy(fromFile, UserFileEntity.class);

        // unlink from-file from from-directory
        var fromDirPath = FileUtil.dirname(fromPath);
        var fromDirectory = userFileDao.find(uid, fromDirPath);
        fromDirectory.getItems().remove(FileUtil.basename(fromFile.getPath()));
        var ops = cassandraTemplate.batchOps()
                .insert(fromDirectory);

        // create new directories for to-directory
        if (toDir == null) {
            Map<String, UserFileEntity> directories = fileChainService
                    .retrospectCreateDirectories(uid, toPath, timestamp);
            toDir = directories.remove(toPath);
            if (!directories.isEmpty()) {
                ops.insert(directories.values());
            }
        }

        // link from-file to to-directory
        String basename = FileUtil.basename(fromPath);
        String toFilePath = FileUtil.normalize(toPath + "/" + basename);
        fromFile.setPath(toFilePath);
        fromFile.setMtime(timestamp);
        fileChainService.associate(toDir, fromFile);

        ops.insert(toDir)
                .insert(fromFile)
                .delete(oldFromFile)
                .execute();
        return RestBody.ok();
    }

    private RestBody<?> moveDirectory(UserFileEntity fromFile, UserFileEntity toDir, UUID uid) {
        var fromPath = fromFile.getPath();
        var toPath = toDir.getPath();
        long timestamp = System.currentTimeMillis();
        var oldFromFile = BeanCopierUtil.copy(fromFile, UserFileEntity.class);

        // unlink from-file from from-directory
        var fromDirPath = FileUtil.dirname(fromPath);
        var fromDir = userFileDao.find(uid, fromDirPath);
        fromDir.getItems().remove(FileUtil.basename(fromPath));
        var ops = cassandraTemplate.batchOps()
                .insert(fromDir);

        // link from-file to to-directory
        var basename = FileUtil.basename(fromPath);
        var toFilePath = FileUtil.normalize(toPath + "/" + basename);
        fromFile.setPath(toFilePath);
        fromFile.setMtime(timestamp);
        fileChainService.associate(toDir, fromFile);

        // recurse
        var subEntities = new ArrayList<UserFileEntity>();
        var oldSubEntities = new ArrayList<UserFileEntity>();

        var items = userFileDao.findAllItems(fromFile);
        for (UserFileEntity item : items) {
            fileChainService.recurseMoveItem(item, fromFile, toDir, basename, subEntities, oldSubEntities);
        }

        ops.insert(toDir)
                .insert(fromFile)
                .delete(oldFromFile)
                .insert(subEntities)
                .delete(oldSubEntities)
                .execute();
        return RestBody.ok();
    }

    @Override
    public RestBody<?> copyFile(String fromPath, String toPath, ServerWebExchange exchange) {
        fromPath = FileUtil.normalize(fromPath);
        toPath = FileUtil.normalize(toPath);
        commonService.checkPath(fromPath, toPath);
        if (toPath.startsWith(fromPath)) {
            throw new ForbiddenException("Target path " + toPath + "is the sub entry of source path " + fromPath);
        }

        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity fromFile = userFileDao.find(uid, fromPath);
        if (fromFile == null) {
            return RestBody.error("Source file %s doesn't exist", fromPath);
        }
        UserFileEntity toDir = userFileDao.find(uid, toPath);
        if (toDir == null) {
            return RestBody.error("Target directory %s doesn't exist", toPath);
        }
        if (commonService.isFile(toDir)) {
            throw new ForbiddenException("Target directory " + toPath + " is a file");
        }
        if (commonService.isDirectory(fromFile)) {
            return copyDiretory(fromFile, toDir, uid);
        }

        UserFileEntity toFile = new UserFileEntity();
        String path = toDir.getPath() + "/" + FileUtil.basename(fromPath);
        path = FileUtil.normalize(path);
        long timestamp = System.currentTimeMillis();
        commonService.fillEntity(toFile, timestamp, uid, path);
        fileChainService.associate(toDir, toFile);
        toFile.setDigest(fromFile.getDigest());
        cassandraTemplate.batchOps()
                .insert(toDir)
                .insert(toFile)
                .execute();
        return RestBody.ok();
    }

    private RestBody<?> copyDiretory(UserFileEntity fromFile, UserFileEntity toDir, UUID uid) {
        var fromPath = fromFile.getPath();
        var toPath = toDir.getPath();
        long timestamp = System.currentTimeMillis();

        String basename = FileUtil.basename(fromPath);
        String toFilePath = FileUtil.normalize(toPath + "/" + basename);
        fromFile.setPath(toFilePath);
        fromFile.setMtime(timestamp);
        fileChainService.associate(toDir, fromFile);

        List<UserFileEntity> subEntities = new ArrayList<>();
        List<UserFileEntity> items = userFileDao.findAllItems(fromFile);
        for (UserFileEntity item : items) {
            fileChainService.recurseMoveItem(item, fromFile, toDir, basename, subEntities, null);
        }

        cassandraTemplate.batchOps()
                .insert(toDir)
                .insert(fromFile)
                .insert(subEntities)
                .execute();
        return RestBody.ok();
    }

    @Override
    public RestBody<?> renameFile(String path, String name, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity entity = userFileDao.find(uid, path);
        if (entity == null) {
            return RestBody.error("File %s doesn't exist", path);
        }
        fileChainService.rename(entity, name);
        return RestBody.ok();
    }
}
