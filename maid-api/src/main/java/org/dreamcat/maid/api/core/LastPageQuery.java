package org.dreamcat.maid.api.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/6/17
 */
@Getter
@Setter
public class LastPageQuery {
    @JsonDeserialize(using = JacksonUtil.LongDeserializer.class)
    @Min(0)
    private Long last;
    @Min(1)
    @Max(1024)
    @NotNull
    private Integer size;
}
