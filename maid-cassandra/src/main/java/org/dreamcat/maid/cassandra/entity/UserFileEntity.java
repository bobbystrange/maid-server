package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

/**
 * Create by tuke on 2020/2/3
 */
@Getter
@Setter
@Table("user_file")
public class UserFileEntity {
    // / if root directory
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String path;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private UUID uid;

    private String digest;
    private String type;
    private Long size;

    private Long ctime;
    private Long mtime;
    private Set<String> items;
}
