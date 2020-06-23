package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.IdQuery;
import org.dreamcat.maid.api.service.FileOpService;
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
@RequestMapping(path = AppConfig.API_PREFIX + "/file", method = RequestMethod.POST)
public class FileOpController {
    private final FileOpService service;

    /**
     * <pre>
     * @api {post} /file/(mkdir|md) Make a directory
     * @apiDescription make a new directory, like `mkdir`
     * @apiName Mkdir
     * @apiGroup FileOp
     * @apiParam {string} id file id
     * @apiParam {string} name file name, max length is 1023, include / to create intermediate directories, max level is 128
     * @apiError (Error 200 code = 1) {number} code file not found
     * @apiError (Error 200 code = 2) {number} code name already exists
     * @apiError (Error 200 code = 2) {string} data the existing file id
     * @apiError (Error 200 code = 3) {number} code operation failed
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": 0,
     *     "name": "a/b/c"
     * }
     * @apiError (Error 401 code = - 1) {number} code `JWT authorization failed, all interfaces excluding auth/share api could throw the exception`
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/mkdir", "md"})
    public Mono<RestBody<Long>> mkdir(
            @Valid @RequestBody Mono<MakeFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.mkdir(it.getId(), it.getName(), exchange));
    }

    /**
     * @api {post} /file/rename Rename a file or directory
     * @apiDescription Rename a specified file or directory
     * @apiName Rename
     * @apiGroup FileOp
     * @apiParam {string} id file id
     * @apiParam {string} name file name, max length is 255
     * @apiError (Error 200 code = 1) {number} code file not found
     * @apiError (Error 200 code = 2) {number} code name already exists
     * @apiError (Error 200 code = 3) {number} code unsupported to rename root
     * @apiError (Error 200 code = 4) {number} code cannot rename to same name
     * @apiError (Error 200 code = 5) {number} code rename failed, maybe deleted during rename
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     */
    @RequestMapping(path = {"/rename"})
    public Mono<RestBody<?>> renameFile(
            @Valid @RequestBody Mono<RenameFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.rename(it.getId(), it.getName(), exchange));
    }

    /**
     * @api {post} /file/(move|mv) Move a file or directory
     * @apiDescription Move a file or directory to a existing dir, like `mv`
     * @apiName Move
     * @apiGroup FileOp
     * @apiParam {string} source source file or directory id
     * @apiParam {string} target target directory id
     * @apiError (Error 200 code = 1) {number} code file not found
     * @apiError (Error 200 code = 2) {number} code unsupported to move root
     * @apiError (Error 200 code = 3) {number} code target dir not found
     * @apiError (Error 200 code = 4) {number} code cannot move to same dir
     * @apiError (Error 200 code = 5) {number} code name already exists
     * @apiError (Error 200 code = 6) {number} code operation failed, maybe deleted during move
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * @apiError (Error 403 code = 2) {number} code insufficient permissions for target file
     */
    @RequestMapping(path = {"/move", "/mv"})
    public Mono<RestBody<?>> moveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.move(it.getSource(), it.getTarget(), exchange));
    }

    /**
     * @api {post} /file/(copy|cp) Copy a file or directory
     * @apiDescription Copy a file or directory to a existing dir, like `cp`
     * @apiName Copy
     * @apiGroup FileOp
     * @apiParam {string} source source file or directory id
     * @apiParam {string} target target directory id
     * @apiError (Error 200 code = 1) {number} code file not found
     * @apiError (Error 200 code = 2) {number} code unsupported to copy root
     * @apiError (Error 200 code = 3) {number} code target dir not found
     * @apiError (Error 200 code = 4) {number} code cannot copy to same dir
     * @apiError (Error 200 code = 5) {number} code operation failed, maybe deleted during copy
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     */
    @RequestMapping(path = {"/copy", "/cp"})
    public Mono<RestBody<?>> copyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.copy(it.getSource(), it.getTarget(), exchange));
    }

    /**
     * @api {post} /file/(remove|rm) Remove a file or directory
     * @apiDescription Remove a specified file, like `rm`
     * @apiName Remove
     * @apiGroup FileOp
     * @apiParam {string} id file id
     * @apiError (Error 200 code = 1) {number} code file not found
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     */
    @RequestMapping(path = {"/remove", "/rm"})
    public Mono<RestBody<?>> remove(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.remove(it.getId(), exchange));
    }

}
