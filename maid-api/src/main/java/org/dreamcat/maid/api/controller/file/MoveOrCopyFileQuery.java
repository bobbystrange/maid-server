package org.dreamcat.maid.api.controller.file;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/3/20
 */
@Data
public class MoveOrCopyFileQuery {
    @NotNull
    private Long fromId;

    @NotNull
    private Long toId;
}
