package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.user.UpdateUserQuery;
import org.dreamcat.maid.api.controller.user.UserView;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/2/6
 */
public interface UserService {

    RestBody<?> deleteUser(ServerWebExchange exchange);

    RestBody<UserView> getUser(ServerWebExchange exchange);

    String getAvatar(ServerWebExchange exchange);

    RestBody<?> updateUser(UpdateUserQuery query, ServerWebExchange exchange);
}
