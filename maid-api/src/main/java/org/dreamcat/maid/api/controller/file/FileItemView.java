package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Create by tuke on 2020/4/14
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileItemView {
    private String name;
    private String path;
    private Long ctime;
    private Long mtime;

    // only file
    private String type;
    private Long size;

    // only dir
    private Integer count;
}
