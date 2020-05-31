package org.dreamcat.maid.cassandra.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

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
        var pid = Long.parseLong(s[0]);

        var fetchSize = properties.getFetchSize();
        var stmt = select("id,name,type")
                .from("user_file")
                .where(eq("uid", uid)).and(eq("pid", pid));
        stmt.setFetchSize(fetchSize);
        var rs = cassandraTemplate.getCqlOperations().queryForResultSet(stmt);
        var iter = rs.iterator();

        List<String> names = new ArrayList<>(fetchSize);
        while (!rs.isFullyFetched()) {
            rs.fetchMoreResults();
            var row = iter.next();
            var id = row.getLong(0);
            var name = row.getString(1);
            var type = row.getString(2);

            if (type != null) {
                setOps.add(uid + ":" + id);
            }

            names.add(name);
            if (names.size() >= fetchSize) {
                userFileDao.deleteByPidAndNames(uid, pid, names);
                names = new ArrayList<>(fetchSize);
            }
        }

        if (ObjectUtil.isNotEmpty(names)) {
            userFileDao.deleteByPidAndNames(uid, pid, names);
        }
    }
}
