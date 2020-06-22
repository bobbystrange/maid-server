package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Create by tuke on 2020/5/28
 */
@Getter
@Setter
public class IdPageQuery extends IdQuery {
    // last file name
    @Size(min = 1, max = 255)
    private String last;

    @Min(1)
    @Max(1024)
    @NotNull
    private Integer size;
}
