package org.dreamcat.maid.cassandra.dao;

import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

/**
 * Create by tuke on 2020/3/23
 */
public interface FileDao extends CassandraRepository<FileEntity, String> {

}
