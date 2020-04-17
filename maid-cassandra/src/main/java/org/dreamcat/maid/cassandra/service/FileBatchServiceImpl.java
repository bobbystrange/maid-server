package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.HttpException;
import org.dreamcat.maid.api.controller.file.MoveOrCopyFileBatchQuery;
import org.dreamcat.maid.api.controller.file.PathBatchView;
import org.dreamcat.maid.api.service.FileBatchService;
import org.dreamcat.maid.api.service.FileOpService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by tuke on 2020/4/14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileBatchServiceImpl implements FileBatchService {
    private final CommonService commonService;
    private final FileOpService fileOpService;

    @Override
    public RestBody<PathBatchView> batchMoveFile(MoveOrCopyFileBatchQuery query, ServerWebExchange exchange) {
        var paths = query.getFromPaths();
        var toPath = query.getToPath();
        commonService.checkPath(toPath);

        var view = newPathBatchView();
        for (var path : paths) {
            try {
                var result = fileOpService.move(path, toPath, exchange);
                if (result.getCode() == 0) {
                    view.getApplied().add(path);
                } else {
                    view.getInvalid().add(path);
                }
            } catch (Exception e) {
                if (e instanceof HttpException) {
                    view.getInvalid().add(path);
                } else {
                    view.getFailed().add(path);
                }
            }
        }
        return RestBody.ok(view);
    }

    @Override
    public RestBody<PathBatchView> batchCopyFile(MoveOrCopyFileBatchQuery query, ServerWebExchange exchange) {
        var paths = query.getFromPaths();
        var toPath = query.getToPath();
        commonService.checkPath(toPath);

        var view = newPathBatchView();
        for (var path : paths) {
            try {
                var result = fileOpService.copy(path, toPath, exchange);
                if (result.getCode() == 0) {
                    view.getApplied().add(path);
                } else {
                    view.getInvalid().add(path);
                }
            } catch (Exception e) {
                if (e instanceof HttpException) {
                    view.getInvalid().add(path);
                } else {
                    view.getFailed().add(path);
                }
            }
        }
        return RestBody.ok(view);
    }

    @Override
    public RestBody<PathBatchView> batchRemoveFile(List<String> paths, ServerWebExchange exchange) {
        if (ObjectUtil.isEmpty(paths)) {
            throw new BadRequestException("expect at least one path in request-body");
        }
        // only support max count is 32
        if (paths.size() > (1 << 5)) {
            throw new ForbiddenException("too many entities, more than 32");
        }

        var view = newPathBatchView();
        for (var path : paths) {
            try {
                var result = fileOpService.remove(path, exchange);
                if (result.getCode() == 0) {
                    view.getApplied().add(path);
                } else {
                    view.getInvalid().add(path);
                }
            } catch (Exception e) {
                if (e instanceof HttpException) {
                    view.getInvalid().add(path);
                } else {
                    view.getFailed().add(path);
                }
            }
        }
        return RestBody.ok(view);
    }

    private PathBatchView newPathBatchView() {
        var view = new PathBatchView();
        view.setApplied(new ArrayList<>());
        view.setFailed(new ArrayList<>());
        view.setInvalid(new ArrayList<>());
        return view;
    }
}
