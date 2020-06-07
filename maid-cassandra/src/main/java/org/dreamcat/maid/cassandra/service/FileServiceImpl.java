package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.controller.file.FileInfoView;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.core.IdLevelQuery;
import org.dreamcat.maid.api.service.FileService;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.dreamcat.maid.cassandra.core.RestCodes.*;

/**
 * Create by tuke on 2020/3/18
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final AppProperties properties;
    private final FileChainService fileChainService;

    @Override
    public RestBody<FileInfoView> file(long fid, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.findById(fid);
        if (file == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!file.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        var view = BeanCopierUtil.copy(file, FileInfoView.class);
        if (commonService.isDirectory(file)) {
            long count = userFileDao.countByPid(uid, fid);
            view.setCount(count);
        }

        return RestBody.ok(view);
    }

    @Override
    public RestBody<List<FileItemView>> list(long pid, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.findById(pid);
        if (dir == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }
        if (!commonService.isDirectory(dir)) {
            return RestBody.error(fid_not_diretory, "fid not diretory");
        }

        long count = userFileDao.countByPid(uid, pid);
        if (count > properties.getFetchSize()) {
            return RestBody.error(excessive_subitems, "excessive subitems");
        }
        var files = userFileDao.findByPid(uid, pid);
        var views = files.stream()
                .map(commonService::toFileItemView)
                .collect(Collectors.toList());
        return RestBody.ok(views);
    }

    @Override
    public RestBody<FileView> tree(long pid, int level, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.findById(pid);
        if (dir == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }
        if (commonService.isFile(dir)) {
            return RestBody.error(fid_not_diretory, "fid not diretory");
        }

        FileView tree = BeanCopierUtil.copy(dir, FileView.class);
        var path = fileChainService.retrievePath(dir);
        tree.setPath(path);
        var batchSize = new AtomicInteger(properties.getBatchSize());
        try {
            fileChainService.recurseTree(tree, dir, level, batchSize);
        } catch (BreakException e) {
            return e.getData();
        }
        return RestBody.ok(tree);
    }

    @Override
    public RestBody<Map<String, List<FileItemView>>> flatTree(long pid, ServerWebExchange exchange) {
        var result = tree(pid, IdLevelQuery.MAX_DIR_LEVEL, exchange);
        if (result.isError()) {
            return RestBody.error(result);
        }

        var view = result.getData();
        var map = new HashMap<String, List<FileItemView>>();
        fileChainService.recurseFlatDirTree(view, map);
        return RestBody.ok(map);
    }

}
