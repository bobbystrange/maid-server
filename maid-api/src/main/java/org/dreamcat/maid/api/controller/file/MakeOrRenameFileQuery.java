package org.dreamcat.maid.api.controller.file;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.IdQuery;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/20
 */
@Getter
@Setter
public class MakeOrRenameFileQuery extends IdQuery {
    @NotEmpty
    @Pattern(regexp = "^[^/]{1,255}$")
    private String name;
}
