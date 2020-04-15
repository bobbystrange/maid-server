package org.dreamcat.maid.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Create by tuke on 2020/3/21
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private FilePath filePath;
    private Rest rest;

    @Data
    public static class FilePath {
        private String tempUpload;
        private String tempUpdate;
        private String upload;
    }

    @Data
    public static class Rest {
        // a key for sign url, using in http(s) API
        private String signKey;
    }
}
