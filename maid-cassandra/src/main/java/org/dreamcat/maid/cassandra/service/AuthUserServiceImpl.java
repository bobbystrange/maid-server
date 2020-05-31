package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.webflux.security.jwt.JwtReactiveFactory;
import org.dreamcat.maid.cassandra.dao.UserDao;
import org.dreamcat.maid.cassandra.entity.UserEntity;
import org.dreamcat.rita.auth.core.RegisterEntity;
import org.dreamcat.rita.auth.service.AuthUserService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

/**
 * Create by tuke on 2020/3/21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthUserServiceImpl implements AuthUserService<UserEntity> {
    private final CassandraTemplate cassandraTemplate;
    private final UserDao userDao;
    private final CommonService commonService;
    private final JwtReactiveFactory jwtFactory;
    private final IdGeneratorService idGeneratorService;

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
        return jwtFactory.generateToken(String.valueOf(user.getId()));
    }

    @Override
    public void createUser(String username, RegisterEntity register) {
        long timestamp = System.currentTimeMillis();
        var uid = idGeneratorService.nextUid();

        var user = BeanCopierUtil.copy(register, UserEntity.class);
        user.setId(uid);
        user.setCtime(timestamp);
        user.setMtime(timestamp);
        user.setName(username);

        // Note that fid equal uid when root directory
        var root = commonService.newUserFile(uid, uid, "/");
        cassandraTemplate.batchOps()
                .insert(user)
                .insert(root)
                .execute();
    }

    @Override
    public void updateUserPassword(UserEntity user, String password) {
        user.setMtime(System.currentTimeMillis());
        user.setPassword(password);
        userDao.save(user);
    }
}
