package org.dreamcat.maid.api.controller.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Create by tuke on 2020/5/30
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfoView extends FileItemView {
    private Long count;
}
