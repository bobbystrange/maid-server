package org.dreamcat.maid.api.service;

import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.page.CategoryQuery;
import org.dreamcat.maid.api.controller.page.ShareItemView;
import org.dreamcat.maid.api.core.LastPageQuery;
import org.dreamcat.maid.api.core.LastPageView;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/6/16
 */
public interface FilePageService {

    RestBody<LastPageView<FileItemView>> category(CategoryQuery query, ServerWebExchange exchange);

    // no check validity of the share file
    RestBody<LastPageView<ShareItemView>> share(LastPageQuery query, ServerWebExchange exchange);

}
