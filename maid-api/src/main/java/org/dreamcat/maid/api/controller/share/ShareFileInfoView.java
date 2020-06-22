package org.dreamcat.maid.api.controller.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.controller.file.FileInfoView;

/**
 * Create by tuke on 2020/6/22
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareFileInfoView extends FileInfoView {
    private Long ttl;
    private Long stime;
}
