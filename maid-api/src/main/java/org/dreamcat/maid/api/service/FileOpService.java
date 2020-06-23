package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/23
 */
public interface FileOpService {

    // mkdir
    RestBody<Long> mkdir(long pid, String name, ServerWebExchange exchange);

    // mv in same directory
    RestBody<?> rename(long fid, String name, ServerWebExchange exchange);

    // mv
    RestBody<?> move(long source, long target, ServerWebExchange exchange);

    // cp
    RestBody<?> copy(long source, long target, ServerWebExchange exchange);

    // rm
    RestBody<?> remove(long fid, ServerWebExchange exchange);

}
