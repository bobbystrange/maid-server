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
public class FileView extends FileItemView {
    private String path;
    private List<FileView> items;
}
