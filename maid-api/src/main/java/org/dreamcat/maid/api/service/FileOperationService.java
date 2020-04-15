package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

/**
 * Create by tuke on 2020/3/23
 */
public interface FileOperationService {

    // cat
    RestBody<String> catFile(String path, ServerWebExchange exchange);

    // mkdir
    RestBody<?> createDirectory(String path, ServerWebExchange exchange);

    // ls -lh
    RestBody<FileView> listDirectory(String path, ServerWebExchange exchange);

    // tree
    RestBody<FileView> listDirectoryTree(String path, ServerWebExchange exchange);

    // path  -->  items
    RestBody<Map<String, List<FileItemView>>> getPathMap(ServerWebExchange exchange);

    // mv
    RestBody<?> moveFile(String fromPath, String toPath, ServerWebExchange exchange);

    // cp -r
    RestBody<?> copyFile(String fromPath, String toPath, ServerWebExchange exchange);

    // mv in same directory
    RestBody<?> renameFile(String path, String name, ServerWebExchange exchange);

}
