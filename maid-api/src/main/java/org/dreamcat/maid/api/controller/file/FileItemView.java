package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.common.web.util.JacksonUtil;

/**
 * Create by tuke on 2020/5/26
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileItemView {
    @JsonSerialize(using = JacksonUtil.LongSerializer.class)
    private Long id;
    @JsonSerialize(using = JacksonUtil.LongSerializer.class)
    private Long pid;
    private String name;

    private Long ctime;
    private Long mtime;
    private String type;
    private Long size;
}
