package org.dreamcat.maid.cassandra.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dreamcat.maid.api.config.AppProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Create by tuke on 2020/5/31
 */
@Getter
@RequiredArgsConstructor
@Configuration
public class RedisConfig {
    private final AppProperties properties;

    private String removeUserFileSet;

    @PostConstruct
    public void init() {
        removeUserFileSet = String.format("%s:remove:user_file:list", properties.getKeyPrefix());
    }
}
