package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathQuery;
import org.dreamcat.maid.api.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/2/3
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file",
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileController {
    private final FileService service;

    @RequestMapping(method = RequestMethod.POST)
    public Mono<RestBody<?>> createFile(
            @Valid @RequestBody Mono<CreateFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.createFile(it, exchange));
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public Mono<RestBody<?>> deleteFile(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.deleteFile(it.getPath(), exchange));
    }

    @RequestMapping(method = RequestMethod.GET)
    public Mono<RestBody<?>> getFile(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.getFile(it.getPath(), exchange));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Mono<RestBody<?>> updateFile(
            @Valid @RequestBody Mono<UpdateFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.updateFile(it, exchange));
    }

}
