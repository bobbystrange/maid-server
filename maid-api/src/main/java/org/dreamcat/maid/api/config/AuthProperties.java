package org.dreamcat.maid.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Create by tuke on 2020/3/11
 */
@Data
@ConfigurationProperties("app.auth")
public class AuthProperties {
    private MaxAge maxAge;

    @Data
    public static class MaxAge {
        private int imageCode;
        private int registerAccessToken;
        private int passwordResetAccessToken;
    }
}
