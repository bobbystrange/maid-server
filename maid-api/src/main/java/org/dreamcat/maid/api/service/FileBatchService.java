package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.MoveOrCopyFileBatchQuery;
import org.dreamcat.maid.api.controller.file.PathBatchView;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * Create by tuke on 2020/4/14
 */
public interface FileBatchService {

    RestBody<PathBatchView> batchMoveFile(MoveOrCopyFileBatchQuery query, ServerWebExchange exchange);

    RestBody<PathBatchView> batchCopyFile(MoveOrCopyFileBatchQuery query, ServerWebExchange exchange);

    RestBody<PathBatchView> batchDeleteFile(List<String> paths, ServerWebExchange exchange);
}
