package org.dreamcat.maid.cassandra.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Create by tuke on 2020/2/6
 */
@Getter
@Setter
@Table("user")
public class UserEntity {
    @PrimaryKey
    private Long id;
    @Indexed
    private String name;

    private Long ctime;
    private Long mtime;
    private String password;
    private String email;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
}
