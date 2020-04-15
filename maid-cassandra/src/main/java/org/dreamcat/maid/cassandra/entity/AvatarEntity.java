package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

/**
 * Create by tuke on 2020/3/21
 */
@Getter
@Setter
@Table("avatar")
public class AvatarEntity {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private UUID uid;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private Long ctime;
    private String avatar;
}
