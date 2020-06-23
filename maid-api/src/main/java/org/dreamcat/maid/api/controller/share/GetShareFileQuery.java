package org.dreamcat.maid.api.controller.share;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Create by tuke on 2020/5/24
 */
@Getter
@Setter
public class GetShareFileQuery {
    @NotNull
    @Min(1 << 22)
    @JsonDeserialize(using = JacksonUtil.LongDeserializer.class)
    private Long sid;
    private String password;
    // null if sid is a file
    @JsonDeserialize(using = JacksonUtil.LongDeserializer.class)
    private Long fid;
}
