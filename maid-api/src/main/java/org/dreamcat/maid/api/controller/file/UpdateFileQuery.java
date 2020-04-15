package org.dreamcat.maid.api.controller.file;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.PathQuery;

import javax.validation.constraints.NotEmpty;

/**
 * Create by tuke on 2020/3/14
 */
@Getter
@Setter
public class UpdateFileQuery extends PathQuery {
    @NotEmpty
    private String content;
}
