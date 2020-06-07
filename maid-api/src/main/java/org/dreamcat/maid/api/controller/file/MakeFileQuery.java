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
public class MakeFileQuery extends IdQuery {
    @NotEmpty
    @Pattern(regexp = "^.{0,1023}$")
    private String name;
}
