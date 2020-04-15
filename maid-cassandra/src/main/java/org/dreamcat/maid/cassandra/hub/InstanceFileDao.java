package org.dreamcat.maid.cassandra.hub;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/26
 */
@Repository
public interface InstanceFileDao extends CassandraRepository<InstanceFileEntity, String> {
    List<InstanceFileEntity> findAllByDigest(String digest);

    void deleteByAddressAndDigest(String address, String digest);

    default List<String> findAddressesByDigest(String digest) {
        return findAllByDigest(digest).stream()
                .map(InstanceFileEntity::getAddress)
                .collect(Collectors.toList());
    }

}
