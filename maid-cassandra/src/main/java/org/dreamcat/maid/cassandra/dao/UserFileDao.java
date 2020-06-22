package org.dreamcat.maid.cassandra.dao;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.core.Pair;
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

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;

/**
 * Create by tuke on 2020/3/17
 */
@RequiredArgsConstructor
@Repository
public class UserFileDao {
    private final CassandraTemplate cassandraTemplate;

    public Long findId(long uid, long pid, String name) {
        var statement = selectFrom("user_file").columns("id")
                .whereColumn("uid").isEqualTo(literal(uid))
                .whereColumn("pid").isEqualTo(literal(pid))
                .whereColumn("name").isEqualTo(literal(name))
                .build();
        return cassandraTemplate.getCqlOperations().queryForObject(statement, Long.class);
    }

    public Pair<Long, String> findPidAndNameById(long id) {
        var statement = selectFrom("user_file").columns("pid", "name")
                .whereColumn("id").isEqualTo(literal(id))
                .build();
        return cassandraTemplate.getCqlOperations().queryForObject(statement,
                (row, rowNum) -> new Pair<>(row.getLong(0), row.getString(1)));
    }

    public UserFileEntity findById(long fid) {
        var query = Query.query(Criteria.where("id").is(fid));
        try {
            return cassandraTemplate.selectOne(query, UserFileEntity.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public UserFileEntity find(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name));
        return cassandraTemplate.selectOne(query, UserFileEntity.class);
    }

    public List<UserFileEntity> findByPid(long uid, long pid) {
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

    public boolean doDelete(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name))
                .queryOptions(DeleteOptions.builder().withIfExists().build());
        return cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void delete(long uid, long pid, String name) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").is(name));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteAllByNames(long uid, long pid, Collection<String> names) {
        var query = Query.query(Criteria.where("uid").is(uid))
                .and(Criteria.where("pid").is(pid))
                .and(Criteria.where("name").in(names));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public void deleteByUid(long uid) {
        var query = Query.query(Criteria.where("uid").is(uid));
        cassandraTemplate.delete(query, UserFileEntity.class);
    }

    public List<UserFileEntity> findLimit(long uid, int size) {
        var query = Query.query(Criteria.where("uid").is(uid)).limit(size);
        return cassandraTemplate.select(query, UserFileEntity.class);
    }

    // Note that it may have unpredictable performance.
    public List<UserFileEntity> findLimit(long uid, int size, Long last) {
        if (last == null) return findLimit(uid, size);

        var statement = selectFrom("user_file")
                .all()
                .whereColumn("uid").isEqualTo(literal(uid))
                .whereColumn("id").isGreaterThan(literal(last))
                .limit(size)
                .allowFiltering()
                .build();
        return cassandraTemplate.select(statement, UserFileEntity.class);
    }

    ///

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

    public boolean doMove(UserFileEntity entity, long newPid) {
        var newEntity = BeanCopierUtil.copy(entity);
        newEntity.setPid(newPid);
        return cassandraTemplate.batchOps()
                .delete(Collections.singleton(entity), DeleteOptions.builder()
                        .withIfExists()
                        .build())
                .insert(Collections.singleton(newEntity), InsertOptions.builder()
                        .withIfNotExists()
                        .build())
                .execute().wasApplied();
    }

    public boolean doCopy(UserFileEntity entity, long newPid, long newFid) {
        var newEntity = BeanCopierUtil.copy(entity);
        newEntity.setId(newFid);
        newEntity.setPid(newPid);
        return cassandraTemplate.batchOps()
                .insert(Collections.singleton(newEntity), InsertOptions.builder()
                        .withIfNotExists()
                        .build())
                .execute().wasApplied();
    }

}
