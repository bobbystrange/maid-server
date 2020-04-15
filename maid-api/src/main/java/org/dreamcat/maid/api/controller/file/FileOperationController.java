package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathQuery;
import org.dreamcat.maid.api.service.FileOperationService;
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
 * Create by tuke on 2020/3/23
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileOperationController {
    private final FileOperationService service;
    private final FileService fileService;

    // only file
    @RequestMapping(path = {"/cat"})
    public Mono<RestBody<?>> catFile(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.catFile(it.getPath(), exchange));
    }

    // only dir
    @RequestMapping(path = {"/mkdir"})
    public Mono<RestBody<?>> createDirectory(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.createDirectory(it.getPath(), exchange));
    }

    // only dir
    @RequestMapping(path = {"/ls", "list"})
    public Mono<RestBody<?>> listDirectory(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.listDirectory(it.getPath(), exchange));
    }

    // only dir
    @RequestMapping(path = {"tree", "/list/tree"})
    public Mono<RestBody<?>> listDirectoryTree(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.listDirectoryTree(it.getPath(), exchange));
    }

    @RequestMapping(path = {"/root/path-map"})
    public Mono<RestBody<?>> getPathMap(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.getPathMap(exchange));
    }

    // both of file and dir
    @RequestMapping(path = "/rename")
    public Mono<RestBody<?>> renameFile(
            @Valid @RequestBody Mono<RenameFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.renameFile(it.getPath(), it.getName(), exchange));
    }

    // file --> new dir or existing dir    &    dir  --> existing dir
    @RequestMapping(path = {"/mv", "/move"})
    public Mono<RestBody<?>> moveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.moveFile(it.getFromPath(), it.getToPath(), exchange));
    }

    // file --> existing dir    &    dir --> existing dir
    @RequestMapping(path = {"/cp", "/copy"})
    public Mono<RestBody<?>> copyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.copyFile(it.getFromPath(), it.getToPath(), exchange));
    }

    // both of file and dir, rm -rf
    @RequestMapping(path = {"/rm", "/remove", "/rmdir"})
    public Mono<RestBody<?>> removeFile(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> fileService.deleteFile(it.getPath(), exchange));
    }

}
