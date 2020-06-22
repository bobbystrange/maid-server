package org.dreamcat.maid.cassandra.task;

import lombok.RequiredArgsConstructor;
import org.dreamcat.maid.cassandra.dao.IdGeneratorDao;
import org.dreamcat.maid.cassandra.entity.IdGeneratorEntity;
import org.dreamcat.maid.cassandra.service.IdGeneratorService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tuke on 2020/6/16
 */
@RequiredArgsConstructor
@Component
public class IdGeneratorRegisterTask {
    private final CassandraTemplate cassandraTemplate;
    private final IdGeneratorDao idGeneratorDao;
    private final IdGeneratorService idGeneratorService;

    @Scheduled(fixedDelay = 10_000)
    public void registerWorkIdPeriodically() {
        if (!idGeneratorService.isInitiated()) return;

        var workId = idGeneratorService.getWorkId();
        var appId = idGeneratorService.getAppId();

        var entity = idGeneratorDao.find(workId);
        // Note that case: reconnect to cassandra or somehow
        if (entity == null) {
            // initiate again
            idGeneratorService.setWorkId(0L);
            idGeneratorService.init();
            return;
        }

        entity = new IdGeneratorEntity();
        entity.setId(workId);
        entity.setAppId(appId);
        cassandraTemplate.insert(entity, InsertOptions.builder()
                .ttl(31_415)
                .build());
    }

}
