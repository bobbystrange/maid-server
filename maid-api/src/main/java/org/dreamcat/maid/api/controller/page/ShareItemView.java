package org.dreamcat.maid.api.controller.page;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

/**
 * Create by tuke on 2020/6/17
 */
@Getter
@Setter
public class ShareItemView {
    @JsonSerialize(using = JacksonUtil.LongSerializer.class)
    private Long id;
    @JsonSerialize(using = JacksonUtil.LongSerializer.class)
    private Long fid;
    private Long ctime;
    private String password;
    private Long ttl;

    private String name;
    private String type;
    private Long size;
}
