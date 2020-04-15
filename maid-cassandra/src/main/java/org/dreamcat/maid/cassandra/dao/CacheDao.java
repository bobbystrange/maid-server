package org.dreamcat.maid.cassandra.dao;

import org.dreamcat.maid.cassandra.entity.CacheEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

/**
 * Create by tuke on 2020/3/22
 */
public interface CacheDao extends CassandraRepository<CacheEntity, String> {
    CacheEntity findByKey(String key);
}
