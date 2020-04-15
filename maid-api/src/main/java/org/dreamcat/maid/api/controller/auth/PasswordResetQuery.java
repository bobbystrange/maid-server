package org.dreamcat.maid.api.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/2/26
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PasswordResetQuery {
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{3,31}$")
    private String username;
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*-_+=/\\\\|:;\"',.?]{4,20}$")
    private String newPassword;
    @Email
    private String email;
    @NotEmpty
    private String imageCode;
    @NotEmpty
    @Pattern(regexp = "^https?://[0-9a-z.-]+?(:[0-9]+?)?/[0-9a-z.-/]*?$")
    private String redirect;
}
