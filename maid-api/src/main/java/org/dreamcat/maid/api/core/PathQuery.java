package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/23
 */
@Getter
@Setter
public class PathQuery {
    // uuid
    public static final String PATTERN_PATH_EXCLUDE_ROOT_STRING =
            "^/.{0,4093}[^/]$";
    public static final java.util.regex.Pattern PATTERN_PATH_EXCLUDE_ROOT = java.util.regex.Pattern.compile(PATTERN_PATH_EXCLUDE_ROOT_STRING);

    @NotEmpty
    @Pattern(regexp = "^/.*$")
    private String path;
}
