package org.dreamcat.maid.api.service;

import org.dreamcat.maid.api.core.RegisterEntity;

/**
 * Create by tuke on 2020/3/21
 */
public interface AuthUserService<T> {
    T findByName(String name);

    String destructureAsEncodedPassword(T user);

    String generateToken(T user);

    void createUser(String username, RegisterEntity register);

    void updateUserPassword(T user, String password);

}
