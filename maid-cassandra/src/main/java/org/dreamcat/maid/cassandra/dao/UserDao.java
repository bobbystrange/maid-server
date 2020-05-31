package org.dreamcat.maid.cassandra.dao;

import org.dreamcat.maid.cassandra.entity.UserEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

/**
 * Create by tuke on 2020/3/17
 */
public interface UserDao extends CassandraRepository<UserEntity, Long> {

    UserEntity findByName(String name);

}
