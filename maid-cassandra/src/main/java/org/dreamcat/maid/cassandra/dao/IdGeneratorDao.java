package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.maid.cassandra.entity.IdGeneratorEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Repository;

/**
 * Create by tuke on 2020/5/31
 */
@RequiredArgsConstructor
@Repository
public class IdGeneratorDao {
    private final CassandraTemplate cassandraTemplate;

    public long nextWorkId(long id) {
        var entity = new IdGeneratorEntity();
        entity.setId(id);
        var res = cassandraTemplate.insert(entity, InsertOptions.builder()
                .withIfNotExists()
                .build());
        if (res.wasApplied()) {
            return -1;
        } else {
            return res.getEntity().getId();
        }
    }
}
