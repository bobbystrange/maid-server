package org.dreamcat.maid.cassandra.hub;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/26
 */
@Repository
public interface InstanceDao extends CassandraRepository<InstanceEntity, String> {

    // sorted from the freest to the fullest
    default List<String> findAlivaAddresses() {
        return findAll().stream()
                .sorted(Comparator.comparingLong(InstanceEntity::getFree).reversed())
                .map(InstanceEntity::getAddress)
                .collect(Collectors.toList());
    }
}
