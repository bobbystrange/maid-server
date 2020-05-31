package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.IdLevelQuery;
import org.dreamcat.maid.api.core.IdQuery;
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
        method = RequestMethod.GET,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileController {
    private final FileService service;

    /**
     * <pre>
     * @api {get} /file/file File file or directory
     * @apiDescription Get file information, like `file` in Unix-like
     * @apiName FileFile
     * @apiGroup File
     * @apiParam {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} pid parent file id
     * @apiSuccess (Success 200 code = 0) {string} name file base name
     * @apiSuccess (Success 200 code = 0) {number} ctime created time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {number} mtime modified time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {number} [count] file subitems count
     * @apiSuccess (Success 200 code = 0) {string} [type] file type, in mime type
     * @apiSuccess (Success 200 code = 0) {number} [size] file size, in bytes
     * @apiError (Error 200 code = 1) code file is not found
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": 2
     * }
     * @apiSuccessExample {json} Success-Response in file case:
     * {
     *     "code": 0,
     *     "data": {
     *         "id": 2,
     *         "pid": 1,
     *         "name": "sample.mp4",
     *         "ctime": 1588166983066,
     *         "mtime": 1588166983066,
     *         "type": "video/mp4",
     *         "size": 1024,
     *     }
     * }
     * @apiSuccessExample {json} Success-Response in directory case:
     * {
     *     "code": 0,
     *     "data": {
     *         "id": 2,
     *         "pid": 1,
     *         "name": "Downloads",
     *         "ctime": 1588166983066,
     *         "mtime": 1588166983066,
     *         "count": 10,
     *     }
     * }
     * @apiErrorExample {json} Error-Response:
     * {
     *     "code": 1,
     *     "msg": "file is not found"
     * }
     * @apiError (Error 403 code = 1) code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/file"})
    public Mono<RestBody<FileInfoView>> file(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.file(it.getId(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/(list|ls) List directory
     * @apiDescription List a specified directory, like `ls`
     * @apiName ListFile
     * @apiGroup File
     * @apiParam {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} pid parent file id
     * @apiSuccess (Success 200 code = 0) {string} name file base name
     * @apiSuccess (Success 200 code = 0) {number} ctime created time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {number} mtime modified time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {string} [type] file type, in mime type
     * @apiSuccess (Success 200 code = 0) {number} [size] file size, in bytes
     * @apiError (Error 200 code = 1) code file is not found
     * @apiError (Error 200 code = 3) code file is not a diretory
     * @apiError (Error 200 code = 4) code sub items number over 1024
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": [
     *         {
     *             "id": 2,
     *             "pid": 1,
     *             "name": "filename",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *             "type": "text/plain",
     *             "size": 1024,
     *         },
     *         {
     *             "id": 3,
     *             "pid": 1,
     *             "name": "folder_name",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *         },
     *     ]
     * }
     * @apiError (Error 403 code = 1) code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/list", "/ls"})
    public Mono<RestBody<List<FileItemView>>> list(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.list(it.getId(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/tree Tree directory
     * @apiDescription List a specified directory tree, like `tree`
     * @apiName TreeFile
     * @apiGroup File
     * @apiParam {number} id file id
     * @apiSuccess (Success 200 code = 0) {string} path file path, root directory is '/'
     * @apiSuccess (Success 200 code = 0) {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} pid parent file id
     * @apiSuccess (Success 200 code = 0) {string} name file base name
     * @apiSuccess (Success 200 code = 0) {number} ctime created time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {number} mtime modified time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {string} [type] file type, in mime type
     * @apiSuccess (Success 200 code = 0) {number} [size] file size, in bytes
     * @apiSuccess (Success 200 code = 0) {array} [items] sub items
     * @apiError (Error 200 code = 1) code file is not found
     * @apiError (Error 200 code = 3) code file is not a diretory
     * @apiError (Error 200 code = 4) code sub items number over 1024
     * @apiError (Error 200 code = 5) code batch size over 65536
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": 2,
     *     "level": 3
     * }
     * @apiSuccessExample (Success 200 code = 0) {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *         "id": 2,
     *         "pid": 1,
     *         "path": "/path/to/folder_name",
     *         "name": "folder_name"
     *         "ctime": 1588166983066,
     *         "mtime": 1588166983066,
     *         "items": [
     *             {
     *                 "id": 3,
     *                 "pid": 2,
     *                 "path": "/path/to/folder_name/filename",
     *                 "name": "filename"
     *                 "ctime": 1588166983066,
     *                 "mtime": 1588166983066,
     *                 "type": "text/plain",
     *                 "size": 1024
     *             }
     *         ]
     *     },
     * }
     * @apiError (Error 403 code = 1) code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/tree"})
    public Mono<RestBody<FileView>> tree(
            @Valid @RequestBody Mono<IdLevelQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.tree(it.getId(), it.getLevel(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/flat/tree Tree flat directory
     * @apiDescription List a specified flat directory tree, error code is same as `ls`
     * @apiName FlatTreeFile
     * @apiGroup File
     * @apiParam {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} id file id
     * @apiSuccess (Success 200 code = 0) {number} pid parent file id
     * @apiSuccess (Success 200 code = 0) {string} name file base name
     * @apiSuccess (Success 200 code = 0) {number} ctime created time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {number} mtime modified time, in milliseconds
     * @apiSuccess (Success 200 code = 0) {string} [type] file type, in mime type
     * @apiSuccess (Success 200 code = 0) {number} [size] file size, in bytes
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *         "/path/to/folder_name": [
     *             {
     *                 "id": 3,
     *                 "pid": 1,
     *                 "name": "filename",
     *                 "ctime": 1588166983066,
     *                 "mtime": 1588166983066,
     *                 "type": "text/plain",
     *                 "size": 1024
     *             }
     *         ]
     *     }
     * }
     * @apiError (Error 403 code = 1) code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/flat/tree"})
    public Mono<RestBody<Map<String, List<FileItemView>>>> listFlatDirTree(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.flatTree(it.getId(), exchange));
    }

}
