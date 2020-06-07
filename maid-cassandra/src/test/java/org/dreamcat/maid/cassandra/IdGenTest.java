package org.dreamcat.maid.cassandra;

import org.dreamcat.common.core.snowflake.IdWorker;
import org.junit.jupiter.api.Test;

/**
 * Create by tuke on 2020/5/31
 */
public class IdGenTest {

    @Test
    public void nextUid() {
        IdWorker userIdWorker = new IdWorker(0, 0, 0);
        System.out.println(userIdWorker.nextId());
    }

}
