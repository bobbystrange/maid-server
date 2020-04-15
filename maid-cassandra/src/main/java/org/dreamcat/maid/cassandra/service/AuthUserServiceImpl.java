package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.common.webflux.security.JwtReactiveFactory;
import org.dreamcat.maid.api.core.RegisterEntity;
import org.dreamcat.maid.api.service.AuthUserService;
import org.dreamcat.maid.cassandra.dao.UserDao;
import org.dreamcat.maid.cassandra.entity.UserEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

/**
 * Create by tuke on 2020/3/21
 */
@RequiredArgsConstructor
@Service
public class AuthUserServiceImpl implements AuthUserService<UserEntity> {
    private final UserDao userDao;
    private final CommonService commonService;
    private final CassandraTemplate cassandraTemplate;
    private final JwtReactiveFactory jwtFactory;

    @Override
    public UserEntity findByName(String name) {
        return userDao.findByName(name);
    }

    @Override
    public String destructureAsEncodedPassword(UserEntity user) {
        return user == null ? null : user.getPassword();
    }

    @Override
    public String generateToken(UserEntity user) {
        return jwtFactory.generateToken(user.getId().toString());
    }

    @Override
    public void createUser(String username, RegisterEntity register) {
        long timestamp = System.currentTimeMillis();
        UserEntity user = BeanCopierUtil.copy(register, UserEntity.class);
        commonService.fillEntity(user, timestamp);
        user.setName(username);

        commonService.createUser(user, timestamp);
    }

    @Override
    public void updateUserPassword(UserEntity user, String password) {
        user.setMtime(System.currentTimeMillis());
        user.setPassword(password);
        userDao.save(user);
    }
}
