package org.dreamcat.maid.api.controller.user;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.IdQuery;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/14
 */
@Getter
@Setter
public class UpdateUserQuery extends IdQuery {
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{3,31}$")
    private String username;
    @Email
    private String email;
    @Pattern(regexp = "^.{0,127}$")
    private String firstName;
    @Pattern(regexp = "^.{0,127}$")
    private String lastName;
}
