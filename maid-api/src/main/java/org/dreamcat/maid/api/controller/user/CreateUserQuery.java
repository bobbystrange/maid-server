package org.dreamcat.maid.api.controller.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/2/6
 */
@Getter
@Setter
public class CreateUserQuery extends UpdateUserQuery {
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*-_+=/\\\\|:;\"',.?]{4,20}$")
    private String password;
}
