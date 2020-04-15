package org.dreamcat.maid.api.controller.file;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.PathQuery;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/20
 */
@Getter
@Setter
public class RenameFileQuery extends PathQuery {
    @NotEmpty
    @Pattern(regexp = "^[^/]{1,127}$")
    private String name;
}
