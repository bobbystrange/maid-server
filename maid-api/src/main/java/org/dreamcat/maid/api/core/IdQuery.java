package org.dreamcat.maid.api.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

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
    public static final long ROOT_ID = 0;
    public static final String ROOT_NAME = "/";

    @Min(0)
    @NotNull
    @JsonDeserialize(using = JacksonUtil.LongDeserializer.class)
    private Long id;
}
