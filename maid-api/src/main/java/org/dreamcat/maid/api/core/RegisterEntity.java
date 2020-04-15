package org.dreamcat.maid.api.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Create by tuke on 2020/3/11
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterEntity {
    private long timestamp;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
