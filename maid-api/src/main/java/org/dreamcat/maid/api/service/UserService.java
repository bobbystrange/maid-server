package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.user.CreateUserQuery;
import org.dreamcat.maid.api.controller.user.UpdateUserQuery;
import org.dreamcat.maid.api.controller.user.UserView;

/**
 * Create by tuke on 2020/2/6
 */
public interface UserService {

    RestBody<?> createUser(CreateUserQuery query);

    RestBody<?> deleteUser(String id);

    RestBody<UserView> getUser(String id);

    RestBody<?> updateUser(UpdateUserQuery query);
}
