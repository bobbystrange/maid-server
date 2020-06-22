package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.FileInfoView;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.controller.file.IdNameView;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

/**
 * Create by tuke on 2020/2/6
 */
public interface FileService {

    // get path by file id
    RestBody<String> path(long fid, ServerWebExchange exchange);

    // file
    RestBody<FileInfoView> file(long fid, ServerWebExchange exchange);

    // ls -lh
    RestBody<List<FileItemView>> list(long pid, ServerWebExchange exchange);

    RestBody<List<FileItemView>> listPage(long pid, String last, int size, ServerWebExchange exchange);

    RestBody<List<IdNameView>> listPath(long fid, ServerWebExchange exchange);

    // tree
    RestBody<FileView> tree(long pid, int level, ServerWebExchange exchange);

    // path  -->  items
    RestBody<Map<String, List<FileItemView>>> flatTree(long pid, ServerWebExchange exchange);

}
