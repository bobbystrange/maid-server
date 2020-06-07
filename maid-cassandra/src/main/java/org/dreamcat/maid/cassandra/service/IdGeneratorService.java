package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.core.snowflake.IdWorker;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.dao.IdGeneratorDao;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Create by tuke on 2020/5/31
 */
@RequiredArgsConstructor
@Service
public class IdGeneratorService {
    private final IdGeneratorDao idGeneratorDao;
    private final AppProperties properties;

    private IdWorker userIdWorker;
    private IdWorker fileIdWorker;
    private IdWorker shareIdWorker;

    @PostConstruct
    public void init() {
        var workId = 0L;
        long lastWordId;
        while ((lastWordId = idGeneratorDao.nextWorkId(workId)) >= 0) {
            workId = lastWordId + 1;
        }

        var datacenterId = properties.getDatacenterId();
        this.userIdWorker = new IdWorker(workId, datacenterId, 0);
        this.fileIdWorker = new IdWorker(workId, datacenterId, 0);
        this.shareIdWorker = new IdWorker(workId, datacenterId, 0);
    }

    public long nextUid() {
        return userIdWorker.nextId();
    }

    public long nextFid() {
        return fileIdWorker.nextId();
    }

    public long nextSid() {
        return shareIdWorker.nextId();
    }
}
