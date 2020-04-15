package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/3/22
 */
@Getter
@Setter
@Table("cache")
public class CacheEntity {
    // one-to-one
    @PrimaryKey
    private String key;
    private String value;
}
