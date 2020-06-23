package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.maid.api.core.IdLevelQuery;
import org.dreamcat.maid.api.service.FileOpService;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
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
    private final CassandraTemplate cassandraTemplate;
    private final CommonService commonService;
    private final StringRedisTemplate redisTemplate;
    private final RedisConfig redisConfig;
    private final IdGeneratorService idGeneratorService;
    private final CacheService cacheService;

    private BoundSetOperations<String, String> copyUserFileSetOps;
    private BoundSetOperations<String, String> removeUserFileSetOps;

    @PostConstruct
    public void init() {
        copyUserFileSetOps = redisTemplate.boundSetOps(redisConfig.getCopyUserFileSet());
        removeUserFileSetOps = redisTemplate.boundSetOps(redisConfig.getRemoveUserFileSet());
    }

    @Override
    public RestBody<Long> mkdir(long pid, String name, ServerWebExchange exchange) {
        if (name.startsWith("/") || name.endsWith("/")) {
            throw new BadRequestException("invalid name");
        }
        // no // in name
        if (name.replaceAll("/+", "/").length() < name.length()) {
            throw new BadRequestException("invalid name");
        }
        var names = name.split("/");
        var len = names.length;
        if (len > IdLevelQuery.MAX_DIR_LEVEL) {
            throw new BadRequestException("invalid name");
        }

        UserFileEntity dir;
        try {
            dir = commonService.checkFid(pid, exchange);
        } catch (BreakException e) {
            return e.getData();
        }

        var uid = dir.getUid();
        for (int i = 0; i < len; i++) {
            var currentName = names[i];
            var file = commonService.newUserFile(uid, pid, currentName);
            //var fid = file.getId();
            var res = cassandraTemplate.insert(file, InsertOptions.builder()
                    .withIfNotExists()
                    .build());
            if (!res.wasApplied()) {
                var oldFile = res.getEntity();
                // already exists
                if (i == len - 1) {
                    return RestBody.error(name_already_exist, "name already exists", oldFile.getId());
                }
                pid = oldFile.getId();
            } else {
                if (i == len - 1) {
                    return RestBody.ok(file.getId());
                }
                pid = file.getId();
            }
        }

        return RestBody.error(mkdir_operation_failed, "operation failed");
    }

    @Override
    public RestBody<?> rename(long fid, String name, ServerWebExchange exchange) {
        UserFileEntity file;
        try {
            file = commonService.checkFid(fid, exchange);
        } catch (BreakException e) {
            return e.getData();
        }

        var uid = file.getUid();
        var oldName = file.getName();
        if (oldName.equals("/")) {
            return RestBody.error(unsupported_root, "unsupported operation for root");
        }
        if (oldName.equals(name)) {
            return RestBody.error(rename_to_same_name, "cannot rename to same name");
        }

        var targetFile = userFileDao.find(uid, file.getPid(), name);
        if (targetFile != null) {
            return RestBody.error(name_already_exist, "name already exists");
        }

        if (!userFileDao.doUpdateName(file, name)) {
            return RestBody.error(rename_operation_failed, "operation failed");
        }
        // update cache
        cacheService.saveFidToPidAndName(uid, fid, file.getPid(), name);
        return RestBody.ok();
    }

    @Override
    public RestBody<?> move(long source, long target, ServerWebExchange exchange) {
        UserFileEntity file;
        try {
            file = commonService.checkFid(source, exchange);
        } catch (BreakException e) {
            return e.getData();
        }

        var uid = file.getUid();
        var name = file.getName();
        if (name.equals("/")) {
            return RestBody.error(unsupported_operation_root, "unsupported to move root");
        }

        try {
            checkTargetFid(file, target);
        } catch (BreakException e) {
            return e.getData();
        }

        var targetFile = userFileDao.find(uid, target, name);
        if (targetFile != null) {
            return RestBody.error(move_name_already_exist, "name already exists");
        }

        if (!userFileDao.doMove(file, target)) {
            return RestBody.error(move_operation_failed, "operation failed");
        }

        cacheService.saveFidToPidAndName(uid, source, target, name);
        return RestBody.ok();
    }

    // Note that no-atomic op for copy a dir
    @Override
    public RestBody<?> copy(long source, long target, ServerWebExchange exchange) {
        UserFileEntity file;
        try {
            file = commonService.checkFid(source, exchange);
        } catch (BreakException e) {
            return e.getData();
        }

        var uid = file.getUid();
        var name = file.getName();
        if (name.equals("/")) {
            return RestBody.error(unsupported_operation_root, "unsupported to copy root");
        }

        UserFileEntity dir;
        try {
            dir = checkTargetFid(file, target);
        } catch (BreakException e) {
            return e.getData();
        }

        if (commonService.isFile(file)) {
            var newFid = idGeneratorService.nextFid();
            if (!userFileDao.doCopy(file, dir.getId(), newFid)) {
                return RestBody.error(copy_operation_failed, "operation failed");
            }
        } else {
            try {
                copyUserFileSetOps.add(String.format("%d:%d:%d", uid, source, target));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new InternalServerErrorException();
            }
        }
        return RestBody.ok();
    }

    // Note that no-atomic op for remove a dir
    @Override
    public RestBody<?> remove(long fid, ServerWebExchange exchange) {
        UserFileEntity file;
        try {
            file = commonService.checkFid(fid, exchange);
        } catch (BreakException e) {
            return e.getData();
        }

        var uid = file.getUid();
        if (!userFileDao.doDelete(uid, file.getPid(), file.getName())) {
            return RestBody.error(fid_not_found, "fid not found");
        }

        if (commonService.isDirectory(file)) {
            try {
                removeUserFileSetOps.add(uid + ":" + fid);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                userFileDao.insert(file);
                throw new InternalServerErrorException();
            }
        }

        cacheService.deleteFidToPidAndName(uid, fid);
        return RestBody.ok();
    }

    private UserFileEntity checkTargetFid(UserFileEntity file, long targetFid) throws BreakException {
        var uid = file.getUid();
        var pid = file.getPid();

        var dir = userFileDao.findById(targetFid);
        if (dir == null) {
            throw new BreakException(RestBody.error(target_fid_not_found, "target fid not found"));
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions_for_target_file, "insufficient permissions for target file");
        }

        if (pid.equals(dir.getId())) {
            throw new BreakException(RestBody.error(unsupported_same_dir, "unsupported operation for same dir"));
        }

        return dir;
    }

}
