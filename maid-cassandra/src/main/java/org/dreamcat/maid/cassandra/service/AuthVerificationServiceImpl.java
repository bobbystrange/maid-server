package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.util.JacksonUtil;
import org.dreamcat.maid.api.config.AuthProperties;
import org.dreamcat.maid.api.core.PasswordResetEntity;
import org.dreamcat.maid.api.core.RegisterEntity;
import org.dreamcat.maid.api.service.AuthVerificationService;
import org.dreamcat.maid.cassandra.dao.CacheDao;
import org.dreamcat.maid.cassandra.entity.CacheEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Service;

/**
 * Create by tuke on 2020/3/21
 */
@RequiredArgsConstructor
@Service
public class AuthVerificationServiceImpl implements AuthVerificationService {
    private static final String AUTH_PROOF_IMAGE_CODE_PREFIX = "maid:cassandra:auth:proof:image_code:";
    private static final String AUTH_USERNAME_REGISTER_PREFIX = "maid:cassandra:auth:username:register:";
    private static final String AUTH_USERNAME_PASSWORD_RESET_PREFIX = "maid:cassandra:auth:username:password_reset:";
    private final CassandraTemplate cassandraTemplate;
    private final CacheDao cacheDao;
    private final AuthProperties authProperties;

    @Override
    public String releaseImageCode(String proof) {
        var kv = cacheDao.findByKey(AUTH_PROOF_IMAGE_CODE_PREFIX + proof);
        return kv == null ? null : kv.getValue();
    }

    @Override
    public void sealImageCode(String proof, String imageCode) {
        var kv = new CacheEntity();
        kv.setKey(AUTH_PROOF_IMAGE_CODE_PREFIX + proof);
        kv.setValue(imageCode);
        cassandraTemplate.insert(kv, InsertOptions.builder()
                .ttl(authProperties.getMaxAge().getImageCode())
                .build());
    }

    @Override
    public RegisterEntity releaseRegister(String username) {
        var kv = cacheDao.findByKey(AUTH_USERNAME_REGISTER_PREFIX + username);
        return kv == null ? null : JacksonUtil.fromJson(kv.getValue(), RegisterEntity.class);
    }

    @Override
    public void sealRegister(String username, RegisterEntity register) {
        var kv = new CacheEntity();
        kv.setKey(AUTH_USERNAME_REGISTER_PREFIX + username);
        kv.setValue(JacksonUtil.toJson(register));
        cassandraTemplate.insert(kv, InsertOptions.builder()
                .ttl(authProperties.getMaxAge().getRegisterAccessToken())
                .build());
    }

    @Override
    public PasswordResetEntity releasePasswordReset(String username) {
        var kv = cacheDao.findByKey(AUTH_USERNAME_PASSWORD_RESET_PREFIX + username);
        return kv == null ? null : JacksonUtil.fromJson(kv.getValue(), PasswordResetEntity.class);
    }

    @Override
    public void sealPasswordReset(String username, PasswordResetEntity passwordReset) {
        var kv = new CacheEntity();
        kv.setKey(AUTH_USERNAME_PASSWORD_RESET_PREFIX + username);
        kv.setValue(JacksonUtil.toJson(passwordReset));
        cassandraTemplate.insert(kv, InsertOptions.builder()
                .ttl(authProperties.getMaxAge().getPasswordResetAccessToken())
                .build());
    }
}
