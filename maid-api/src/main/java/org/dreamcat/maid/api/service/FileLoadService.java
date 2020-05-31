package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.ShareFileQuery;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/20
 */
public interface FileLoadService {

    RestBody<?> upload(long pid, FilePart filePart, ServerWebExchange exchange);

    RestBody<String> download(long fid, boolean attachment, ServerWebExchange exchange);

    RestBody<Long> share(ShareFileQuery query, ServerWebExchange exchange);

}
