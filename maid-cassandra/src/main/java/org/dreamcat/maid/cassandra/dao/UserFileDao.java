package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/17
 */
@RequiredArgsConstructor
@Repository
public class UserFileDao {
    private final CassandraTemplate cassandraTemplate;

    public UserFileEntity find(UUID uid, String path) {
        Query query = Query.query(Criteria.where("path").is(path))
                .and(Criteria.where("uid").is(uid));
        return cassandraTemplate.selectOne(query, UserFileEntity.class);
    }

    public List<UserFileEntity> findAllItems(UserFileEntity directory) {
        if (ObjectUtil.isEmpty(directory.getItems())) {
            return Collections.emptyList();
        }
        return findAll(directory.getUid(), directory.getItems(), directory.getPath());
    }

    public List<UserFileEntity> findAll(UUID uid, Collection<String> names, String directoryPath) {
        List<String> paths = names.stream()
                .map(it -> FileUtil.normalize(directoryPath + "/" + it))
                .collect(Collectors.toList());
        return findAll(uid, paths);
    }

    public List<UserFileEntity> findAll(UUID uid, List<String> paths) {
        Query query = Query.query(Criteria.where("path").in(paths))
                .and(Criteria.where("uid").is(uid));
        return cassandraTemplate.select(query, UserFileEntity.class);
    }

    public void saveAll(List<UserFileEntity> entities) {
        cassandraTemplate.batchOps().insert(entities).execute();
    }

    public void delete(UserFileEntity entity) {
        cassandraTemplate.delete(entity);
    }

    public void delete(UUID uid, String path) {
        Query query = Query.query(Criteria.where("path").is(path))
                .and(Criteria.where("uid").is(uid));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteAll(Collection<UserFileEntity> entities) {
        cassandraTemplate.batchOps().delete(entities).execute();
    }

    public void deleteAll(UUID uid, List<String> paths) {
        Query query = Query.query(Criteria.where("path").in(paths))
                .and(Criteria.where("uid").is(uid));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }
}
