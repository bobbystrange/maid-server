package org.dreamcat.maid.cassandra.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.hc.okhttp.OkHttpWget;
import org.dreamcat.maid.api.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;

/**
 * Create by tuke on 2020/3/30
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class BootstrapConfig {
    private final AppProperties appProperties;

    @Bean
    public OkHttpWget wget() {
        return new OkHttpWget(true);
    }

    @PostConstruct
    public void init() {
        Arrays.asList(
                appProperties.getFilePath().getTempUpload(),
                appProperties.getFilePath().getTempUpdate(),
                appProperties.getFilePath().getUpload()
        ).forEach(it -> {
            File file = new File(it);
            if (file.mkdirs()) {
                log.info("Succeeded to mkdir {}", it);
            } else if (!file.exists()) {
                log.error("Failed to mkdir {}", it);
            }
        });
    }
}
