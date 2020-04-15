package org.dreamcat.maid.cassandra.hub;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/3/26
 */
@Data
@Table("instance")
public class InstanceEntity {
    // RPC address which providers RPC API
    @PrimaryKey
    private String address;

    // HTTP(s) port which providers download/upload API
    @Column("server_port")
    private Short serverPort;

    // HTTP(s) public domain
    @Indexed
    private String domain;

    // disk free size
    private Long free;
    // files used sized
    private Long used;
    // file count
    private Long count;
}
