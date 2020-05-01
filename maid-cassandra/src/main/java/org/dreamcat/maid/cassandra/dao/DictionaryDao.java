package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.maid.cassandra.entity.DictionaryEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Create by tuke on 2020/3/22
 */
@RequiredArgsConstructor
@Repository
public class DictionaryDao {
    private final CassandraTemplate cassandraTemplate;

    public DictionaryEntity find(String key, String name) {
        var query = Query.query(Criteria.where("key").is(key))
                .and(Criteria.where("name").is(name));
        return cassandraTemplate.selectOne(query, DictionaryEntity.class);
    }
}
