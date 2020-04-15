package org.dreamcat.maid.cassandra.hub;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/3/26
 */
@Data
@Table("instance_file")
public class InstanceFileEntity {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String digest;
    @PrimaryKeyColumn
    private String address;
}
