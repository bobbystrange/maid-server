package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Create by tuke on 2020/3/19
 */
@Getter
@Setter
public class IdQuery {
    // uuid
    public static final String PATTERN_ID_STRING =
            "^[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}$";
    public static final java.util.regex.Pattern PATTERN_ID = java.util.regex.Pattern.compile(PATTERN_ID_STRING);

    @NotEmpty
    @Pattern(regexp = PATTERN_ID_STRING)
    private String id;
}
