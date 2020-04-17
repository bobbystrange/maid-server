package org.dreamcat.maid.api.controller.file;

import lombok.Data;
import org.dreamcat.maid.api.core.PathQuery;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/20
 */
@Data
public class MoveOrCopyFileQuery {
    @NotEmpty
    @Pattern(regexp = PathQuery.PATTERN_PATH_EXCLUDE_ROOT_STRING)
    private String fromPath;

    @NotEmpty
    @Pattern(regexp = PathQuery.PATTERN_PATH_STRING)
    private String toPath;
}
