package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/5/31
 */
@Getter
@Setter
@Table("id_generator")
public class IdGeneratorEntity {
    @PrimaryKey
    private Long id;
}
