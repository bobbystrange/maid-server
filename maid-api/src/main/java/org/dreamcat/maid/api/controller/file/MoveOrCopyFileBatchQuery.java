package org.dreamcat.maid.api.controller.file;

import lombok.Data;
import org.dreamcat.maid.api.core.PathQuery;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Create by tuke on 2020/4/14
 */
@Data
public class MoveOrCopyFileBatchQuery {
    @NotEmpty
    @Size(min = 2, max = 32)
    private List<String> fromPaths;

    @NotEmpty
    @Pattern(regexp = PathQuery.PATTERN_PATH_EXCLUDE_ROOT_STRING)
    private String toPath;
}
