package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/2/3
 */
@Getter
@Setter
@Table("user_file")
public class UserFileEntity {
    // user id
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private Long uid;
    // parent file id
    @PrimaryKeyColumn
    private Long pid;
    // filename
    @PrimaryKeyColumn
    private String name;
    // file id
    @Indexed
    private Long id;

    private Long ctime;
    private Long mtime;
    private String digest;
    private String type;
    private Long size;

}
