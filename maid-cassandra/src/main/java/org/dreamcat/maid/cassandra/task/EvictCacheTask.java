package org.dreamcat.maid.cassandra.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;


/**
 * Create by tuke on 2020/6/9
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EvictCacheTask {
    private final StringRedisTemplate redisTemplate;
    private final CassandraTemplate cassandraTemplate;
    private final RedisConfig redisConfig;
    private final AppProperties properties;

    @PostConstruct
    public void init() {
        // clear cache while bootstrap
        clearFidToPathByUsers();
        clearFidToPathByRedisKeys();
    }

    private void clearFidToPathByUsers() {
        var fetchSize = properties.getFetchSize();
        var statement = selectFrom("user")
                .columns("id")
                .build()
                .setPageSize(fetchSize);

        var rs = cassandraTemplate.getCqlOperations().queryForResultSet(statement);
        var iter = rs.iterator();

        List<String> keys = new ArrayList<>(fetchSize);
        while (!rs.isFullyFetched()) {
            var row = iter.next();
            var uid = row.getLong(0);

            keys.add(redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid);
            if (keys.size() >= fetchSize) {
                redisTemplate.delete(keys);
                keys.clear();
            }
        }

        if (ObjectUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private void clearFidToPathByRedisKeys() {
        var keys = redisTemplate.keys(redisConfig.getMapFidToPidAndNameHashPrefix() + ":*");
        if (ObjectUtil.isEmpty(keys)) return;
        redisTemplate.delete(keys);
    }
}
