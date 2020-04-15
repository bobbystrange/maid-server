package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Create by tuke on 2020/3/22
 */
@Getter
@Setter
public class ImageCodeView {
    private String source;
    private long timestamp;
    private int nonce;
}
