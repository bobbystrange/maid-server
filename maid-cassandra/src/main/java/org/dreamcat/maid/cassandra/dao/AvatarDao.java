package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.maid.cassandra.entity.AvatarEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Create by tuke on 2020/3/21
 */
@RequiredArgsConstructor
@Repository
public class AvatarDao {
    private final CassandraTemplate cassandraTemplate;

    public AvatarEntity find(Long uid) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .sort(Sort.by(Sort.Direction.DESC, "ctime"));
        return cassandraTemplate.selectOne(query, AvatarEntity.class);
    }

    public void delete(Long uid) {
        Query query = Query.query(Criteria.where("uid").is(uid));
        cassandraTemplate.delete(query, AvatarEntity.class);
    }
}
