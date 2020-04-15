package org.dreamcat.maid.cassandra.hub;

import lombok.Getter;
import lombok.Setter;

/**
 * Create by tuke on 2020/4/13
 */
@Getter
@Setter
public class DownloadQuery extends UploadQuery {
    private String type;
    private String filename;
}
