package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/4/15
 */
@Getter
@Setter
public class IdLevelQuery extends IdQuery {
    public static final int MAX_DIR_LEVEL = 128;

    @NotNull
    @Max(MAX_DIR_LEVEL)
    @Min(1)
    private Integer level;
}
