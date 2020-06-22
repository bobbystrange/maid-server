package org.dreamcat.maid.cassandra.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.service.CacheService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;

/**
 * Create by tuke on 2020/5/31
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RemoveUserFileTask {
    private final UserFileDao userFileDao;
    private final CassandraTemplate cassandraTemplate;
    private final AppProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RedisConfig redisConfig;
    private final CacheService cacheService;

    private BoundSetOperations<String, String> setOps;

    @PostConstruct
    public void init() {
        setOps = redisTemplate.boundSetOps(redisConfig.getRemoveUserFileSet());
    }

    @Scheduled(fixedDelay = 1_000)
    public void receive() {
        String message;
        while ((message = setOps.randomMember()) != null) {
            recurseRemove(message);
            setOps.remove(message);
        }
    }

    public void recurseRemove(String message) {
        var s = message.split(":");
        var uid = Long.parseLong(s[0]);
        var pid = Long.parseLong(s[1]);

        var fetchSize = properties.getFetchSize();
        var stmt = selectFrom("user_file")
                .columns("id", "name", "type")
                .whereColumn("uid").isEqualTo(literal(uid))
                .whereColumn("pid").isEqualTo(literal(pid))
                .build()
                .setPageSize(fetchSize);

        var rs = cassandraTemplate.getCqlOperations().queryForResultSet(stmt);
        var iter = rs.iterator();

        List<String> names = new ArrayList<>(fetchSize);
        int offset = 0;
        Object[] ids = new Long[fetchSize];
        while (!rs.isFullyFetched()) {
            if (names.size() >= fetchSize) {
                userFileDao.deleteAllByNames(uid, pid, names);
                cacheService.deleteFidToPidAndName(uid, ids);
                names.clear();
                offset = 0;
            }

            var row = iter.next();
            var id = row.getLong(0);
            var name = row.getString(1);
            var type = row.getString(2);

            if (type != null) {
                setOps.add(uid + ":" + id);
            }

            names.add(name);
            ids[offset++] = id;
        }

        if (ObjectUtil.isNotEmpty(names)) {
            userFileDao.deleteAllByNames(uid, pid, names);
        }
        if (offset > 0) {
            cacheService.deleteFidToPidAndName(uid, Arrays.copyOf(ids, offset));
        }
    }
}
