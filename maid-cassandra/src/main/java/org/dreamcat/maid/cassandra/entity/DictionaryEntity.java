package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/3/22
 */
@Getter
@Setter
@Table("dictionary")
public class DictionaryEntity {
    // many-to-many
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String key;
    @PrimaryKeyColumn
    private String name;
    private String value;
}
