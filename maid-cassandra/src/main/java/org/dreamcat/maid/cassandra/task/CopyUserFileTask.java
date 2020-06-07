package org.dreamcat.maid.cassandra.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.service.CommonService;
import org.dreamcat.maid.cassandra.service.IdGeneratorService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 * Create by tuke on 2020/6/7
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CopyUserFileTask {
    private final CommonService commonService;
    private final UserFileDao userFileDao;
    private final CassandraTemplate cassandraTemplate;
    private final IdGeneratorService idGeneratorService;
    private final AppProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RedisConfig redisConfig;

    private BoundSetOperations<String, String> setOps;

    @PostConstruct
    public void init() {
        setOps = redisTemplate.boundSetOps(redisConfig.getCopyUserFileSet());
    }

    @Scheduled(fixedDelay = 1_000)
    public void receive() {
        String message;
        while ((message = setOps.randomMember()) != null) {
            recurseCopy(message);
            setOps.remove(message);
        }
    }

    public void recurseCopy(String message) {
        var s = message.split(":");
        var uid = Long.parseLong(s[0]);
        var fromFid = Long.parseLong(s[1]);
        var toPid = Long.parseLong(s[2]);

        var fromFile = userFileDao.findById(fromFid);
        if (fromFile == null) {
            log.warn("file was already deleted while copy {} to {}", fromFid, toPid);
            return;
        }
        var newFid = idGeneratorService.nextFid();
        if (!userFileDao.doCopy(fromFile, toPid, newFid)) {
            log.warn("copy failed from {} to {}", fromFid, toPid);
            return;
        }
        if (commonService.isFile(fromFile)) return;

        var fetchSize = properties.getFetchSize();
        var stmt = select("id")
                .from("user_file")
                .where(eq("uid", uid)).and(eq("pid", fromFid));
        stmt.setFetchSize(fetchSize);
        var rs = cassandraTemplate.getCqlOperations().queryForResultSet(stmt);
        var iter = rs.iterator();

        while (!rs.isFullyFetched()) {
            rs.fetchMoreResults();
            var row = iter.next();
            var id = row.getLong(0);
            setOps.add(uid + ":" + id + ":" + newFid);
        }

    }
}
