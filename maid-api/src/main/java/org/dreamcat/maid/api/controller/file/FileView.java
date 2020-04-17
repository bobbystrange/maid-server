package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Create by tuke on 2020/3/14
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileView {
    private String path;
    private String name;
    private Long ctime;
    private Long mtime;
    // only file
    // Note that for internal usage only
    //private String digest;
    private String type;
    private Long size;
    // only dir
    private List<FileView> items;
}
