package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/5/28
 * <p>
 * ^[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}$
 */
@Getter
@Setter
public class IdQuery {
    @NotNull
    @Min(1 << 22)
    private Long id;
}
