package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.dao.ShareFileDao;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;


/**
 * Create by tuke on 2020/5/31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileBatchService {
    private final ShareFileDao shareFileDao;
    private final CassandraTemplate cassandraTemplate;
    private final AppProperties properties;

    public void deleteAllShareFile(long uid) {
        var fetchSize = properties.getFetchSize();
        var stmt = selectFrom("share_file")
                .columns("id")
                .whereColumn("uid").isEqualTo(literal(uid))
                .build()
                .setPageSize(fetchSize);

        var rs = cassandraTemplate.getCqlOperations().queryForResultSet(stmt);
        var iter = rs.iterator();

        List<Long> ids = new ArrayList<>(fetchSize);
        while (!rs.isFullyFetched()) {
            var row = iter.next();
            var id = row.getLong(0);

            ids.add(id);
            if (ids.size() >= fetchSize) {
                shareFileDao.deleteByIds(ids);
                ids = new ArrayList<>(fetchSize);
            }
        }

        if (ObjectUtil.isNotEmpty(ids)) {
            shareFileDao.deleteByIds(ids);
        }
    }


}
