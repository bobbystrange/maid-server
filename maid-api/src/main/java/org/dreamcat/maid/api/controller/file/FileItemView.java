package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Create by tuke on 2020/5/26
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileItemView {
    private String id;
    private String pid;
    private String name;

    private Long ctime;
    private Long mtime;
    private String type;
    private Long size;
}
