package org.dreamcat.maid.api.controller.file;

import lombok.Getter;
import lombok.Setter;
import org.dreamcat.maid.api.core.IdQuery;

/**
 * Create by tuke on 2020/5/24
 */
@Getter
@Setter
public class ShareFileQuery extends IdQuery {
    private String password;
    private Long ttl;
}
