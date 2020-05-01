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

    /**
     * @api {post} /file/(list|ls) List a directory
     * @apiDescription list a specified directory
     * @apiName ListFile
     * @apiGroup File
     * @apiParam {string} path directory path
     * @apiSuccess {number} code
     * @apiSuccess {string} name filename
     * @apiSuccess {string} path file path, root directory is '/'
     * @apiSuccess {number} ctime created time in milliseconds
     * @apiSuccess {number} mtime modified time in milliseconds
     * @apiSuccess [string] type file type, in mime type
     * @apiSuccess [number] size file size, in bytes
     * @apiSuccess [number] count sub items count
     * @apiSuccessExample {json} Success-Response:
     * {
     * "code": 0,
     * "data": [
     * {
     * "name": "filename",
     * "path": "/path/to/filename",
     * "ctime": 1588166983066,
     * "mtime": 1588166983066,
     * "type": "text/plain",
     * "size": 1024,
     * },
     * {
     * "name": "folder_name",
     * "path": "/path/to/folder_name",
     * "ctime": 1588166983066,
     * "mtime": 1588166983066,
     * "count": 3,
     * },
     * ]
     * }
     * @apiError (Error 403) {Number} code -1, path is not a diretory
     * @apiError (Error 404) {Number} code -1, path is not found
     */
    @RequestMapping(path = {"/list", "/ls"})
    public Mono<RestBody<List<FileItemView>>> list(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.list(it.getPath(), exchange));
    }

    /**
     * @api {post} /file/tree List a directory tree
     * @apiDescription list a specified directory tree
     * @apiName TreeFile
     * @apiGroup File
     * @apiParam {string} path directory path
     * @apiSuccess {number} code
     * @apiSuccess {string} name filename
     * @apiSuccess {string} path file path, root directory is '/'
     * @apiSuccess {number} ctime created time in milliseconds
     * @apiSuccess {number} mtime modified time in milliseconds
     * @apiSuccess [string] type file type, in mime type
     * @apiSuccess [number] size file size, in bytes
     * @apiSuccess [object array] items sub items
     * @apiSuccessExample {json} Success-Response:
     * {
     * "code": 0,
     * "data": {
     * "name": "folder_name",
     * "path": "/path/to/folder_name",
     * "ctime": 1588166983066,
     * "mtime": 1588166983066,
     * "items": [
     * {
     * "name": "filename",
     * "path": "/path/to/folder_name/filename",
     * "ctime": 1588166983066,
     * "mtime": 1588166983066,
     * "type": "text/plain",
     * "size": 1024
     * }
     * ]
     * },
     * }
     * @apiError (Error 403) {Number} code -1, path is not a diretory
     * @apiError (Error 404) {Number} code -1, path is not found
     */
    @RequestMapping(path = {"/tree"})
    public Mono<RestBody<FileView>> tree(
            @Valid @RequestBody Mono<PathLevelQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.tree(it.getPath(), it.getLevel(), exchange));
    }

    /**
     * @api {post} /file/flat/tree List a flat directory tree
     * @apiDescription list a specified flat directory tree
     * @apiName FlatTreeFile
     * @apiGroup File
     * @apiParam {string} path directory path
     * @apiSuccess {number} code
     * @apiSuccess {string} name filename
     * @apiSuccess {string} path file path, root directory is '/'
     * @apiSuccess {number} ctime created time in milliseconds
     * @apiSuccess {number} mtime modified time in milliseconds
     * @apiSuccess [string] type file type, in mime type
     * @apiSuccess [number] size file size, in bytes
     * @apiSuccess [object array] items sub items
     * @apiSuccessExample {json} Success-Response:
     * {
     * "code": 0,
     * "data": {
     * "/path/to/folder_name": [
     * {
     * "name": "filename",
     * "path": "/path/to/folder_name/filename",
     * "ctime": 1588166983066,
     * "mtime": 1588166983066,
     * "type": "text/plain",
     * "size": 1024
     * },
     * ],
     * },
     * }
     * @apiError (Error 403) {Number} code -1, path is not a diretory
     * @apiError (Error 404) {Number} code -1, path is not found
     */
    @RequestMapping(path = {"/flat/tree"})
    public Mono<RestBody<Map<String, List<FileItemView>>>> listFlatDirTree(
            @Valid @RequestBody Mono<PathQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.flatTree(it.getPath(), exchange));
    }

}
