package org.dreamcat.maid.api.controller.file;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/3/20
 */
@Data
public class MoveFileQuery {
    @NotNull
    @Min(1 << 22)
    private Long fromId;

    @NotNull
    @Min(1 << 22)
    private Long toId;
}
