package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathQuery;
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
@RequestMapping(path = AppConfig.API_PREFIX + "/file",
        method = RequestMethod.POST)
public class FileOpController {
    private final FileOpService service;

    /**
     * @api {post} /file/(mkdir|md) Make directory
     * @apiDescription make a new directory
     * @apiName Mkdir
     * @apiGroup File
     * @apiParam {string} path directory path
     * @apiParam {string} file file in multipart/form-data
     * @apiSuccess (Success 200) {Number} code 0, succeed to mkdir or already exists
     * @apiError (Error 400) {Number} code -1, invalid path, all interfaces including path parameter could throw the exception
     * @apiError (Error 401) {Number} code -1, JWT authorization failed, all interfaces excluding auth api could throw the exception
     */
    @RequestMapping(path = {"/mkdir", "md"})
    public Mono<RestBody<?>> mkdir(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.mkdir(it.getPath(), exchange));
    }

    /**
     * @api {post} /file/rename Rename file or directory
     * @apiDescription rename a specified file or directory
     * @apiName RenameFile
     * @apiGroup File
     * @apiParam {string} path file or directory path
     * @apiParam {string{2..256}} name new name
     * @apiSuccess (Success 200) {Number} code 0
     * @apiError (Error 403) {Number} code -1, unsupported to rename root directory, target path already exists or too many entities (expect less than or equals 1024)
     * @apiError (Error 404) {Number} code -1, path doesn't exist
     */
    @RequestMapping(path = {"/rename"})
    public Mono<RestBody<?>> renameFile(
            @Valid @RequestBody Mono<RenameFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.rename(it.getPath(), it.getName(), exchange));
    }

    /**
     * @api {post} /file/(move|mv) Move file or directory
     * @apiDescription move a specified file to a new dir or existing dir, or move a directory to existing dir
     * @apiName MoveFile
     * @apiGroup File
     * @apiParam {string} fromPath source file or directory path
     * @apiParam {string} toPath target directory path, must exist when fromPath is a directory
     * @apiSuccess (Success 200) {Number} code 0,
     * @apiError (Error 403) {Number} code -1, target is the sub entry of source, target is a file or too many entities (expect less than or equals 1024)
     * @apiError (Error 404) {Number} code -1, source doesn't exist, source is a directory and target doesn't exist
     */
    @RequestMapping(path = {"/move", "/mv"})
    public Mono<RestBody<?>> moveFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.move(it.getFromPath(), it.getToPath(), exchange));
    }

    /**
     * @api {post} /file/(copy|cp) Copy file or directory
     * @apiDescription copy a specified file to a new dir or existing dir, or move a directory to existing dir
     * @apiName CopyFile
     * @apiGroup File
     * @apiParam {string} fromPath source file or directory path
     * @apiParam {string} toPath target directory path, must exist when fromPath is a directory
     * @apiSuccess (Success 200) {Number} code 0,
     * @apiError (Error 403) {Number} code -1, target is the sub entry of source, target is a file or too many entities (expect less than or equals 1024)
     * @apiError (Error 404) {Number} code -1, source doesn't exist, source is a directory and target doesn't exist
     */
    @RequestMapping(path = {"/copy", "/cp"})
    public Mono<RestBody<?>> copyFile(
            @Valid @RequestBody Mono<MoveOrCopyFileQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.copy(it.getFromPath(), it.getToPath(), exchange));
    }

    /**
     * @api {post} /file/(remove|rm) Remove file or directory
     * @apiDescription remove a specified file
     * @apiName RemoveFile
     * @apiGroup File
     * @apiParam {string} path file or directory path
     * @apiSuccess (Success 200) {Number} code 0,
     * @apiError (Error 403) {Number} code -1, too many entities (expect less than or equals 1024)
     * @apiError (Error 404) {Number} code -1, path doesn't exist
     */
    @RequestMapping(path = {"/remove", "/rm"})
    public Mono<RestBody<?>> remove(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.remove(it.getPath(), exchange));
    }

    /**
     * @api {post} /file/(remove|rm)/numerous Remove file or directory(no limit)
     * @apiDescription it doesn't guarantee the atomicity of the operation
     * @apiName RemoveNumerousFile
     * @apiGroup File
     * @apiParam {string} path file or directory path
     * @apiSuccess (Success 200) {Number} code 0,
     * @apiError (Error 404) {Number} code -1, path doesn't exist
     */
    @RequestMapping(path = {"/remove/numerous", "/rm/numerous"})
    public Mono<RestBody<?>> removeNumerous(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.removeNumerous(it.getPath(), exchange));
    }

}
