package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.CreateFileQuery;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.controller.file.UpdateFileQuery;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/2/6
 */
public interface FileService {
    // touch
    RestBody<String> createFile(CreateFileQuery query, ServerWebExchange exchange);

    // rm -rf
    RestBody<?> deleteFile(String path, ServerWebExchange exchange);

    // cat
    RestBody<FileView> getFile(String path, ServerWebExchange exchange);

    // echo >
    RestBody<?> updateFile(UpdateFileQuery query, ServerWebExchange exchange);
}
