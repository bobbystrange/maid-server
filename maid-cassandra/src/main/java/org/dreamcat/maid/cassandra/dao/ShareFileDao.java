package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.maid.cassandra.entity.ShareFileEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Create by tuke on 2020/5/24
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ShareFileDao {
    private final CassandraTemplate cassandraTemplate;

    public ShareFileEntity findById(long id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return cassandraTemplate.selectOne(query, ShareFileEntity.class);
    }

    public void deleteById(long id) {
        Query query = Query.query(Criteria.where("id").is(id));
        cassandraTemplate.delete(query, ShareFileEntity.class);
    }

    public void deleteByIds(Collection<Long> ids) {
        Query query = Query.query(Criteria.where("id").in(ids));
        cassandraTemplate.delete(query, ShareFileEntity.class);
    }
}
