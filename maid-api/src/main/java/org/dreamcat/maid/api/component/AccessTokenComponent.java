package org.dreamcat.maid.api.component;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.crypto.SignUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.jwt.JwtProperties;
import org.dreamcat.common.web.util.JacksonUtil;
import org.springframework.stereotype.Component;

/**
 * Create by tuke on 2020/3/11
 */
@RequiredArgsConstructor
@Component
public class AccessTokenComponent {
    private final JwtProperties jwtProperties;

    public <T> String encode(T data) {
        return SignUtil.hs512Base64(
                JacksonUtil.toJson(data), jwtProperties.getSecretKey());
    }

    public <T> boolean isInvalid(T data, String accessToken) {
        ObjectUtil.requireNotBlank(accessToken, "data");
        String signature = SignUtil.hs512Base64(
                JacksonUtil.toJson(data), jwtProperties.getSecretKey());
        return !accessToken.equals(signature);
    }
}
