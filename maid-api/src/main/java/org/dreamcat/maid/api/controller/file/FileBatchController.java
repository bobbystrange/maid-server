package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.service.FileBatchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

/**
 * Create by tuke on 2020/4/14
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file/batch",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileBatchController {
    private final FileBatchService service;

    @RequestMapping(path = {"/mv", "move"})
    public Mono<RestBody<?>> batchMoveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileBatchQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.batchMoveFile(it, exchange));
    }

    @RequestMapping(path = {"/cp", "/copy"})
    public Mono<RestBody<?>> batchCopyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileBatchQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.batchCopyFile(it, exchange));
    }

    @RequestMapping(path = {"/rm", "/remove", "rmdir", "/remove/dir"})
    public Mono<RestBody<?>> batchRemoveFile(
            @Valid @RequestBody Mono<List<String>> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.batchRemoveFile(it, exchange));
    }
}
