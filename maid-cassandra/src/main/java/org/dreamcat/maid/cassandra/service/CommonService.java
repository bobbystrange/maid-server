package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.exception.UnauthorizedException;
import org.dreamcat.common.webflux.security.jwt.JwtReactiveFactory;
import org.dreamcat.maid.api.controller.file.FileInfoView;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommonService {
    private final JwtReactiveFactory jwtFactory;
    private final UserFileDao userFileDao;
    private final IdGeneratorService idGeneratorService;

    public long retrieveUid(ServerWebExchange exchange) {
        var subject = jwtFactory.getSubject(exchange);
        if (subject == null) {
            throw new UnauthorizedException("No token in current request");
        }
        return Long.parseLong(subject);
    }

    public UserFileEntity newUserFile(long uid, long pid, String name) {
        var fid = idGeneratorService.nextFid();
        var timestmap = System.currentTimeMillis();

        var file = new UserFileEntity();
        file.setId(fid);
        file.setUid(uid);
        file.setPid(pid);
        file.setName(name);
        file.setCtime(timestmap);
        file.setMtime(timestmap);
        return file;
    }

    public boolean isFile(UserFileEntity file) {
        return file.getType() != null;
    }

    public boolean isDirectory(UserFileEntity file) {
        return !isFile(file);
    }

    public FileItemView toFileItemView(UserFileEntity userFile) {
        return BeanCopierUtil.copy(userFile, FileItemView.class);
    }

    public FileItemView toFileItemView(FileView fileView) {
        return BeanCopierUtil.copy(fileView, FileItemView.class);
    }

    public FileInfoView toFileInfoView(UserFileEntity userFile) {
        var view = BeanCopierUtil.copy(userFile, FileInfoView.class);
        if (isDirectory(userFile)) {
            long count = userFileDao.countByPid(userFile.getUid(), userFile.getId());
            view.setCount(count);
        }
        return view;
    }

}
