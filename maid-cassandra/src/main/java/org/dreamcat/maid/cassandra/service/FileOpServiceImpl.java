package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.maid.api.service.FileOpService;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.PostConstruct;

import static org.dreamcat.maid.cassandra.core.RestCodes.*;

/**
 * Create by tuke on 2020/3/23
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileOpServiceImpl implements FileOpService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final StringRedisTemplate redisTemplate;
    private final RedisConfig redisConfig;

    private BoundSetOperations<String, String> setOps;

    @PostConstruct
    public void init() {
        setOps = redisTemplate.boundSetOps(redisConfig.getRemoveUserFileSet());
    }

    @Override
    public RestBody<?> mkdir(long pid, String name, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.findById(pid);
        if (dir == null) {
            return RestBody.error(parent_fid_not_found, "parent fid not found");
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        var file = commonService.newUserFile(uid, pid, name);
        if (!userFileDao.doInsert(file)) {
            return RestBody.error(name_already_exist, "name already exists");
        }
        return RestBody.ok();
    }

    @Override
    public RestBody<?> rename(long fid, String name, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.findById(fid);
        if (file == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!file.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        var oldName = file.getName();
        if (oldName.equals("/")) {
            return RestBody.error(unsupported_root, "unsupported to rename root directory /");
        }
        if (oldName.equals(name)) {
            return RestBody.error(rename_to_same_name, "cannot rename to same name");
        }

        var targetFile = userFileDao.findByPidAndName(uid, file.getPid(), name);
        if (targetFile != null) {
            return RestBody.error(name_already_exist, "name already exists");
        }

        if (!userFileDao.doUpdateName(file, name)) {
            return RestBody.error(rename_failed, "rename failed");
        }
        return RestBody.ok();
    }

    // Note that no-atomic op
    @Override
    public RestBody<?> move(long fromId, long toId, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.findById(fromId);
        if (file == null) {
            return RestBody.error(fid_not_found, "fid is not found");
        }
        if (!file.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        var pid = file.getPid();
        var name = file.getName();
        if (name.equals("/")) {
            return RestBody.error(unsupported_root, "unsupported to move root directory /");
        }

        var dir = userFileDao.findById(toId);
        if (dir == null) {
            return RestBody.error(target_fid_not_found, "target fid is not found");
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        if (pid.equals(dir.getId())) {
            return RestBody.error(move_to_same_dir, "cannot move to same dir");
        }

        var targetFile = userFileDao.findByPidAndName(uid, dir.getId(), name);
        if (targetFile != null) {
            return RestBody.error(name_already_exist, "name already exists");
        }

        file.setPid(dir.getId());
        if (!userFileDao.doInsert(file)) {
            return RestBody.error(move_failed, "move failed");
        }

        userFileDao.deleteByPidAndName(uid, pid, name);
        return RestBody.ok();
    }

    // Note that no-atomic op for remove a dir
    @Override
    public RestBody<?> remove(long fid, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.findById(fid);
        if (file == null) {
            return RestBody.error(fid_not_found, "fid is not found");
        }
        if (!file.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        if (!userFileDao.doDeleteByPidAndName(uid, file.getPid(), file.getName())) {
            return RestBody.error(fid_not_found, "fid not found");
        }

        if (commonService.isDirectory(file)) {
            try {
                setOps.add(uid + ":" + fid);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                userFileDao.insert(file);
                return RestBody.error(remove_failed, "remove failed");
            }
        }

        return RestBody.ok();
    }

}
