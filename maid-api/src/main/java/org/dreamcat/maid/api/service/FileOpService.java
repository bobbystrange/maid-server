package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/23
 */
public interface FileOpService {

    // mkdir
    RestBody<?> mkdir(String path, ServerWebExchange exchange);

    // mv in same directory
    RestBody<?> rename(String path, String name, ServerWebExchange exchange);

    // mv
    RestBody<?> move(String fromPath, String toPath, ServerWebExchange exchange);

    // cp -r
    RestBody<?> copy(String fromPath, String toPath, ServerWebExchange exchange);


    RestBody<?> remove(String path, ServerWebExchange exchange);

}
