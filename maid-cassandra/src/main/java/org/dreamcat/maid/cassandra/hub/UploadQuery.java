package org.dreamcat.maid.cassandra.hub;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/4/12
 */
@Data
public class UploadQuery {
    @NotBlank
    private String digest;
    @NotNull
    private Integer nonce;
    @NotNull
    @Min(1 << 16)
    private Long timestamp;
    @NotBlank
    private String sign;
}
