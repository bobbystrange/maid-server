package org.dreamcat.maid.api.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Create by tuke on 2020/6/17
 */
@Getter
@Setter
public class LastPageView<T> {
    private List<T> items;
    private Long last;
    private boolean hasMore;
}
