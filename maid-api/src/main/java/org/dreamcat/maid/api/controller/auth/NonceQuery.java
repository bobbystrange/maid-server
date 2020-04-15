package org.dreamcat.maid.api.controller.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Create by tuke on 2020/3/22
 */
@Getter
@Setter
public class NonceQuery {
    @Size(min = 4, max = 255)
    @NotEmpty
    private String proof;
    @NotNull
    private Long timestamp;
    @NotNull
    private Integer nonce;
}
