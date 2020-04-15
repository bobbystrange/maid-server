package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/3/23
 */
@Getter
@Setter
@Table("file")
public class FileEntity {
    // message digest
    @PrimaryKey
    private String digest;
    // MediaType
    private String type;
    // Bytes
    private Long size;
}
