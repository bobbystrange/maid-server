package org.dreamcat.maid.cassandra.dao;

import org.dreamcat.maid.cassandra.entity.AvatarEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * Create by tuke on 2020/3/21
 */
public interface AvatarDao extends CassandraRepository<AvatarEntity, UUID> {
}
