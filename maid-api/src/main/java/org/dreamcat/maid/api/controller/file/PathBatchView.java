package org.dreamcat.maid.api.controller.file;

import lombok.Data;

import java.util.List;

/**
 * Create by tuke on 2020/4/14
 */
@Data
public class PathBatchView {
    // always failed
    private List<String> invalid;
    // maybe success
    private List<String> failed;
    // success
    private List<String> applied;

}
