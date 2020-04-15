package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/20
 */
public interface FileLoadService {

    RestBody<?> uploadFile(String directoryPath, FilePart filePart, ServerWebExchange exchange);

    RestBody<String> downloadFile(String path, ServerWebExchange exchange);

    RestBody<String> shareFile(String id);

}
