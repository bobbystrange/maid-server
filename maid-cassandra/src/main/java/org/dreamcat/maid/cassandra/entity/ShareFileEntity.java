package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/5/24
 */
@Getter
@Setter
@Table("share_file")
public class ShareFileEntity {
    @PrimaryKey
    private Long id;
    @Indexed
    private Long uid;
    @Indexed
    private Long fid;

    private Long ctime;
    private String password;
    private Long ttl;
}
