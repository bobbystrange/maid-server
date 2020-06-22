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
    private String copyUserFileSet;
    private String mapFidToPidAndNameHashPrefix;

    @PostConstruct
    public void init() {
        removeUserFileSet = String.format("%s:remove:user_file:set", properties.getKeyPrefix());
        copyUserFileSet = String.format("%s:copy:user_file:set", properties.getKeyPrefix());
        mapFidToPidAndNameHashPrefix = String.format("%s:cache:fid2pid_name:hash", properties.getKeyPrefix());

    }
}
