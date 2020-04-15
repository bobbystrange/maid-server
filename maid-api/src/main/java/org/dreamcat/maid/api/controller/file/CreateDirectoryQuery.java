package org.dreamcat.maid.api.controller.file;

import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/23
 */
public class CreateDirectoryQuery {
    @Pattern(regexp = "^/.{1,4095}$")
    private String path;
}
