package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathQuery;
import org.dreamcat.maid.api.service.FileOpService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/3/23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileOpController {
    private final FileOpService service;

    // only dir
    @RequestMapping(path = {"/mkdir"})
    public Mono<RestBody<?>> mkdir(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.mkdir(it.getPath(), exchange));
    }

    // both of file and dir
    @RequestMapping(path = "/rename")
    public Mono<RestBody<?>> renameFile(
            @Valid @RequestBody Mono<RenameFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.rename(it.getPath(), it.getName(), exchange));
    }

    // file --> new dir or existing dir    &    dir  --> existing dir
    @RequestMapping(path = {"/mv", "/move"})
    public Mono<RestBody<?>> moveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.move(it.getFromPath(), it.getToPath(), exchange));
    }

    // file --> existing dir    &    dir --> existing dir
    @RequestMapping(path = {"/cp", "/copy"})
    public Mono<RestBody<?>> copyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.copy(it.getFromPath(), it.getToPath(), exchange));
    }

    // both of file and dir, rm -rf
    @RequestMapping(path = {"/rm", "/remove", "/rmdir"})
    public Mono<RestBody<?>> remove(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.remove(it.getPath(), exchange));
    }

}
