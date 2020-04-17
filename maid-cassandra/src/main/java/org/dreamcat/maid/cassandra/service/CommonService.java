package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.UnauthorizedException;
import org.dreamcat.common.webflux.security.JwtReactiveFactory;
import org.dreamcat.maid.api.core.PathLevelQuery;
import org.dreamcat.maid.api.core.PathQuery;
import org.dreamcat.maid.cassandra.entity.UserEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Create by tuke on 2020/3/20
 */
@RequiredArgsConstructor
@Service
public class CommonService {
    private final CassandraTemplate cassandraTemplate;
    private final JwtReactiveFactory jwtFactory;

    public UUID retrieveUid(ServerWebExchange exchange) {
        String subject = jwtFactory.getSubject(exchange);
        if (subject == null) {
            throw new UnauthorizedException("No subject in current request");
        }
        return UUID.fromString(subject);
    }

    public UserFileEntity rootFileEntity(UUID uid, long timestamp) {
        UserFileEntity rootDirectory = new UserFileEntity();
        fillEntity(rootDirectory, timestamp, uid, "/");
        return rootDirectory;
    }

    public void createUser(UserEntity user, long timestamp) {
        UUID uid = user.getId();
        UserFileEntity rootDirectory = rootFileEntity(uid, timestamp);
        cassandraTemplate.batchOps()
                .insert(user, rootDirectory, InsertOptions.builder()
                        .ifNotExists(true).build())
                .execute();
    }

    public void fillEntity(UserEntity user, long timestamp) {
        user.setId(UUID.randomUUID());
        user.setCtime(timestamp);
        user.setMtime(timestamp);
    }

    public void fillEntity(UserFileEntity file, long timestamp, UUID uid, String path) {
        file.setCtime(timestamp);
        file.setMtime(timestamp);
        file.setUid(uid);
        file.setPath(path);
    }

    public boolean isFile(UserFileEntity file) {
        return file.getDigest() != null;
    }

    public boolean isDirectory(UserFileEntity file) {
        return !isFile(file);
    }

    public boolean hasPreview(UserFileEntity file) {
        var type = file.getType();
        if (ObjectUtil.isBlank(type)) return false;
        return type.matches("^image/.+$") ||
                type.matches("^video/.+$");
    }

    // avoid over flow stack
    public void checkPath(String path) {
        if (PathQuery.PATTERN_PATH.matcher(path).matches() &&
                path.split("/").length <= PathLevelQuery.MAX_DIR_LEVEL + 1) {
            return;
        }
        throw new BadRequestException("Invalid path");
    }

    public void checkPaths(String... paths) {
        if (ObjectUtil.isEmpty(paths)) return;

        for (String path : paths) {
            checkPath(path);
        }
    }

}
