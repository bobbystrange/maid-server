package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.core.Pair;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.maid.api.service.FileOpService;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create by tuke on 2020/3/23
 */
@RequiredArgsConstructor
@Service
public class FileOpServiceImpl implements FileOpService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final FileChainService fileChainService;
    private final CassandraTemplate cassandraTemplate;

    @Override
    public RestBody<?> mkdir(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.find(uid, path);
        if (file != null) {
            return RestBody.ok();
        }
        fileChainService.mkdir(path, uid);
        return RestBody.ok();
    }

    @Override
    public RestBody<?> rename(String path, String name, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var entity = userFileDao.find(uid, path);
        if (entity == null) {
            throw new NotFoundException("File " + path + " doesn't exist");
        }

        if ("/".equals(path)) {
            throw new ForbiddenException("Unsupported to rename root directory /");
        }

        var parentPath = FileUtil.dirname(path);
        var newPath = FileUtil.normalize(parentPath + "/" + name);
        if (userFileDao.find(uid, newPath) != null) {
            throw new ForbiddenException("File " + newPath + " already exists");
        }

        var timestamp = System.currentTimeMillis();
        var ops = cassandraTemplate.batchOps();
        var opsCounter = new AtomicInteger(1 << 10 - 3);

        var parentDir = userFileDao.find(uid, parentPath);
        var oldName = FileUtil.basename(path);
        parentDir.getItems().remove(oldName);
        parentDir.getItems().add(name);
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
        return RestBody.ok();
    }

    private void recurseRename(UserFileEntity parent, UserFileEntity entity, CassandraBatchOperations ops, AtomicInteger opsCounter) {
        if (opsCounter.get() < 0) {
            throw new ForbiddenException("Too many entities (expect less than or equals 1024)");
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

    @Override
    public RestBody<?> move(String fromPath, String toPath, ServerWebExchange exchange) {
        fileChainService.moveOrCopy(fromPath, toPath, exchange, true);
        return RestBody.ok();
    }

    @Override
    public RestBody<?> copy(String fromPath, String toPath, ServerWebExchange exchange) {
        fileChainService.moveOrCopy(fromPath, toPath, exchange, false);
        return RestBody.ok();
    }

    @Override
    public RestBody<?> remove(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var entity = userFileDao.find(uid, path);
        if (entity == null) {
            throw new NotFoundException("File " + path + " doesn't exist");
        }

        var ops = cassandraTemplate.batchOps();
        var opsCounter = new AtomicInteger(1024);

        // need update parent dir's items;
        if (!path.equals("/")) {
            var basename = FileUtil.basename(path);
            var dirname = FileUtil.dirname(path);
            var dir = userFileDao.find(uid, dirname);
            var items = dir.getItems();
            items.remove(basename);
            dir.setItems(items);
            ops.update(dir);
            opsCounter.decrementAndGet();
        }

        if (commonService.isFile(entity)) {
            ops.delete(entity).execute();
            return RestBody.ok();
        }

        recurseRemove(entity, ops, opsCounter);
        ops.execute();
        return RestBody.ok();
    }

    private void recurseRemove(UserFileEntity entity, CassandraBatchOperations ops, AtomicInteger opsCounter) {
        // avoid oom
        if (opsCounter.get() <= 0) {
            throw new ForbiddenException("Too many entities (expect less than or equals 1024)");
        }
        ops.delete(entity);
        opsCounter.decrementAndGet();

        if (commonService.isFile(entity)) return;

        var items = userFileDao.findAllItems(entity);
        for (UserFileEntity item : items) {
            recurseRemove(item, ops, opsCounter);
        }
    }

    @Override
    public RestBody<?> removeNumerous(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var entity = userFileDao.find(uid, path);
        if (entity == null) {
            throw new NotFoundException("File " + path + " doesn't exist");
        }

        var ops = cassandraTemplate.batchOps();
        var opsCounter = new AtomicInteger(1024);

        // need update parent dir's items;
        if (!path.equals("/")) {
            var basename = FileUtil.basename(path);
            var dirname = FileUtil.dirname(path);
            var dir = userFileDao.find(uid, dirname);
            var items = dir.getItems();
            items.remove(basename);
            dir.setItems(items);
            ops.update(dir);
            opsCounter.decrementAndGet();
        }

        if (commonService.isFile(entity)) {
            ops.delete(entity).execute();
            return RestBody.ok();
        }

        var pair = new Pair<>(ops, opsCounter);
        recurseRemoveNumerous(entity, pair);
        pair.first().execute();
        return RestBody.ok();
    }

    private void recurseRemoveNumerous(UserFileEntity entity, Pair<CassandraBatchOperations, AtomicInteger> pair) {
        // avoid oom
        if (pair.second().get() <= 0) {
            pair.first().execute();
            var newOps = cassandraTemplate.batchOps();
            pair.setFirst(newOps);
            pair.second().set(1 << 10);
        }
        pair.first().delete(entity);
        pair.second().decrementAndGet();

        if (commonService.isFile(entity)) return;

        var items = userFileDao.findAllItems(entity);
        for (UserFileEntity item : items) {
            recurseRemoveNumerous(item, pair);
        }
    }
}
