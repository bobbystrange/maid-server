package org.dreamcat.maid.api.controller.page;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.core.LastPageQuery;
import org.dreamcat.maid.api.core.LastPageView;
import org.dreamcat.maid.api.service.FilePageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/6/16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file/page",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FilePageController {
    private final FilePageService service;

    @RequestMapping(path = {"/category"})
    public Mono<RestBody<LastPageView<FileItemView>>> category(
            @Valid @RequestBody Mono<CategoryQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.category(it, exchange));
    }

    @RequestMapping(path = {"/share"})
    public Mono<RestBody<LastPageView<ShareItemView>>> share(
            @Valid @RequestBody Mono<LastPageQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.share(it, exchange));
    }


}
