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
     * @api {post} /file/(mkdir|md) Make directory
     * @apiDescription make a new directory, like `mkdir`
     * @apiName Mkdir
     * @apiGroup FileOp
     * @apiParam {number} id file id
     * @apiParam {string} name file name, max length is 1023, include / to create intermediate directories, max level is 128
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code file not found
     * @apiError (Error 200 code = 2) code name already exists
     * @apiError (Error 200 code = 2) data the existing file id
     * @apiError (Error 200 code = 3) code operation failed
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": 0,
     *     "name": "a/b/c"
     * }
     * @apiError (Error 401 code = - 1) code `JWT authorization failed, all interfaces excluding auth/share api could throw the exception`
     * @apiError (Error 403 code = 1) code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/mkdir", "md"})
    public Mono<RestBody<Long>> mkdir(
            @Valid @RequestBody Mono<MakeFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.mkdir(it.getId(), it.getName(), exchange));
    }

    /**
     * @api {post} /file/rename Rename file or directory
     * @apiDescription Rename a specified file or directory
     * @apiName RenameFile
     * @apiGroup FileOp
     * @apiParam {number} id file id
     * @apiParam {string} name file name, max length is 255
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code file not found
     * @apiError (Error 200 code = 2) code name already exists
     * @apiError (Error 200 code = 3) code unsupported to rename root
     * @apiError (Error 200 code = 4) code cannot rename to same name
     * @apiError (Error 200 code = 5) rename failed, maybe deleted during rename
     * @apiError (Error 403 code = 1) code insufficient permissions
     */
    @RequestMapping(path = {"/rename"})
    public Mono<RestBody<?>> renameFile(
            @Valid @RequestBody Mono<RenameFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.rename(it.getId(), it.getName(), exchange));
    }

    /**
     * @api {post} /file/(move|mv) Move file or directory
     * @apiDescription Move file or directory to a existing dir, like `mv`
     * @apiName MoveFile
     * @apiGroup FileOp
     * @apiParam {number} fromId source file or directory id
     * @apiParam {number} toId target directory id
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code file not found
     * @apiError (Error 200 code = 2) code unsupported to move root
     * @apiError (Error 200 code = 3) code target dir not found
     * @apiError (Error 200 code = 4) code cannot move to same dir
     * @apiError (Error 200 code = 5) code name already exists
     * @apiError (Error 200 code = 6) code operation failed, maybe deleted during move
     * @apiError (Error 403 code = 1) code insufficient permissions
     * @apiError (Error 403 code = 2) code insufficient permissions for target file
     */
    @RequestMapping(path = {"/move", "/mv"})
    public Mono<RestBody<?>> moveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.move(it.getFromId(), it.getToId(), exchange));
    }

    /**
     * @api {post} /file/(move|mv) Copy file or directory
     * @apiDescription Copy file or directory to a existing dir, like `cp`
     * @apiName CopyFile
     * @apiGroup FileOp
     * @apiParam {number} fromId source file or directory id
     * @apiParam {number} toId target directory id
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code file not found
     * @apiError (Error 200 code = 2) code unsupported to copy root
     * @apiError (Error 200 code = 3) code target dir not found
     * @apiError (Error 200 code = 4) code cannot copy to same dir
     * @apiError (Error 200 code = 5) code operation failed, maybe deleted during copy
     * @apiError (Error 403 code = 1) code insufficient permissions
     */
    @RequestMapping(path = {"/copy", "/cp"})
    public Mono<RestBody<?>> copyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.copy(it.getFromId(), it.getToId(), exchange));
    }

    /**
     * @api {post} /file/(remove|rm) Remove file or directory
     * @apiDescription remove a specified file, like `rm`
     * @apiName RemoveFile
     * @apiGroup FileOp
     * @apiParam {number} id file id
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code file not found
     * @apiError (Error 403 code = 1) code insufficient permissions
     */
    @RequestMapping(path = {"/remove", "/rm"})
    public Mono<RestBody<?>> remove(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.remove(it.getId(), exchange));
    }

}
