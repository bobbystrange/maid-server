package org.dreamcat.maid.api.controller.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/2/26
 */
@Setter
@Getter
@ToString
public class LoginQuery extends NonceQuery {
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{3,31}$")
    private String username;
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*-_+=/\\\\|:;\"',.?]{4,20}$")
    private String password;
    @NotEmpty
    private String imageCode;
}
