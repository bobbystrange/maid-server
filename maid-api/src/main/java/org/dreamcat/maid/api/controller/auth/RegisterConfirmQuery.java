package org.dreamcat.maid.api.controller.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/10
 */
@Data
public class RegisterConfirmQuery {
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{3,31}$")
    private String username;
    @NotEmpty
    private String accessToken;
}
