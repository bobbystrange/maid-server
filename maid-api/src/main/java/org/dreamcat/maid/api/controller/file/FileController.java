package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathLevelQuery;
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
import java.util.List;
import java.util.Map;

/**
 * Create by tuke on 2020/2/3
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileController {
    private final FileService service;

    // only dir
    @RequestMapping(path = {"/ls", "/list"})
    public Mono<RestBody<List<FileItemView>>> list(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.list(it.getPath(), exchange));
    }

    // only dir
    @RequestMapping(path = {"/tree"})
    public Mono<RestBody<FileView>> tree(
            @Valid @RequestBody Mono<PathLevelQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.tree(it.getPath(), it.getLevel(), exchange));
    }

    @RequestMapping(path = {"/flat/tree"})
    public Mono<RestBody<Map<String, List<FileItemView>>>> listFlatDirTree(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.flatTree(it.getPath(), exchange));
    }

}
