package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.controller.user.CreateUserQuery;
import org.dreamcat.maid.api.controller.user.UpdateUserQuery;
import org.dreamcat.maid.api.controller.user.UserView;
import org.dreamcat.maid.api.service.UserService;
import org.dreamcat.maid.cassandra.dao.UserDao;
import org.dreamcat.maid.cassandra.entity.UserEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Create by tuke on 2020/3/18
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final CassandraTemplate cassandraTemplate;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final CommonService commonService;

    @Override
    public RestBody<?> createUser(CreateUserQuery query) {
        long timestamp = System.currentTimeMillis();
        UserEntity user = BeanCopierUtil.copy(query, UserEntity.class);
        commonService.fillEntity(user, timestamp);
        user.setName(query.getUsername());
        user.setPassword(passwordEncoder.encode(query.getPassword()));

        commonService.createUser(user, timestamp);
        return RestBody.ok();
    }

    @Override
    public RestBody<?> deleteUser(String id) {
        userDao.deleteById(UUID.fromString(id));
        return RestBody.ok();
    }

    @Override
    public RestBody<UserView> getUser(String id) {
        UserEntity user = userDao.findById(UUID.fromString(id)).orElse(null);
        if (user == null) return RestBody.error("User %s doesn't exist", id);
        UserView view = BeanCopierUtil.copy(user, UserView.class);
        view.setId(user.getId().toString());
        view.setUsername(user.getName());
        return RestBody.ok(view);
    }

    @Override
    public RestBody<?> updateUser(UpdateUserQuery query) {
        String id = query.getId();
        UserEntity user = userDao.findById(UUID.fromString(id)).orElse(null);
        if (user == null) return RestBody.error("User %s doesn't exist", id);
        BeanCopierUtil.copy(query, user);
        userDao.save(user);
        return RestBody.ok();
    }

}
