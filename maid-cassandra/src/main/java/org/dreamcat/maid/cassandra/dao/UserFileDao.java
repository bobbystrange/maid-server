package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.DeleteOptions;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Create by tuke on 2020/3/17
 */
@RequiredArgsConstructor
@Repository
public class UserFileDao {
    private final CassandraTemplate cassandraTemplate;

    public UserFileEntity findById(long fid) {
        var query = Query.query(Criteria.where("id").is(fid));
        try {
            return cassandraTemplate.selectOne(query, UserFileEntity.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public UserFileEntity findByPidAndName(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name));
        return cassandraTemplate.selectOne(query, UserFileEntity.class);
    }

    public List<? extends UserFileEntity> findAllByPid(long uid, long pid) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid));
        return cassandraTemplate.select(query, UserFileEntity.class);
    }

    public long countByPid(long uid, long pid) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid));
        return cassandraTemplate.count(query, UserFileEntity.class);
    }

    public void insert(UserFileEntity entity) {
        cassandraTemplate.insert(entity);
    }

    public boolean doInsert(UserFileEntity entity) {
        return cassandraTemplate.insert(entity, InsertOptions.builder()
                .withIfNotExists()
                .build()).wasApplied();
    }

    public boolean doUpdateName(UserFileEntity entity, String name) {
        var newEntity = BeanCopierUtil.copy(entity);
        newEntity.setName(name);
        return cassandraTemplate.batchOps()
                .delete(Collections.singleton(entity), DeleteOptions.builder()
                        .withIfExists()
                        .build())
                .insert(Collections.singleton(newEntity), InsertOptions.builder()
                        .withIfNotExists()
                        .build())
                .execute().wasApplied();
    }

    public boolean doDeleteByPidAndName(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name))
                .queryOptions(DeleteOptions.builder().withIfExists().build());
        return cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteByPidAndName(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteByPidAndNames(long uid, long pid, Collection<String> names) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").in(names));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteByUid(long uid) {
        var query = Query.query(Criteria.where("uid").is(uid));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }
}
