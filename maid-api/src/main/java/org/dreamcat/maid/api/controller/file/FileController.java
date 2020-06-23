package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.IdLevelQuery;
import org.dreamcat.maid.api.core.IdPageQuery;
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
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FileController {
    private final FileService service;

    /**
     * <pre>
     * @api {get} /file/path Get path by file id
     * @apiDescription Get path by file id
     * @apiName Path
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiSuccess {string} data file path, look like /a/b/c/d
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": "0"
     * }
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": "/a/b/c/d"
     * }
     * @apiErrorExample {json} Error-Response:
     * {
     *     "code": 1,
     *     "message": "fid not found"
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/path"})
    public Mono<RestBody<String>> path(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.path(it.getId(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/file Get file information
     * @apiDescription Get Get file information, like `file` in Unix-like
     * @apiName File
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiSuccess {string} id file id
     * @apiSuccess {string} pid parent file id
     * @apiSuccess {string} name file base name
     * @apiSuccess {number} ctime created time, in milliseconds
     * @apiSuccess {number} mtime modified time, in milliseconds
     * @apiSuccess {number} [count] file subitems count
     * @apiSuccess {string} [type] file type, in mime type
     * @apiSuccess {number} [size] file size, in bytes
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": "2"
     * }
     * @apiSuccessExample {json} Success-Response in file case:
     * {
     *     "code": 0,
     *     "data": {
     *         "id": "2",
     *         "pid": "1",
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
     *         "id": "2",
     *         "pid": "1",
     *         "name": "Downloads",
     *         "ctime": 1588166983066,
     *         "mtime": 1588166983066,
     *         "count": 10,
     *     }
     * }
     * @apiErrorExample {json} Error-Response:
     * {
     *     "code": 1,
     *     "message": "file not found"
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
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
     * @api {get} /file/(list|ls) List a directory
     * @apiDescription List a specified directory, like `ls`
     * @apiName List
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiSuccess {string} id file id
     * @apiSuccess {string} pid parent file id
     * @apiSuccess {string} name file base name
     * @apiSuccess {number} ctime created time, in milliseconds
     * @apiSuccess {number} mtime modified time, in milliseconds
     * @apiSuccess {string} [type] file type, in mime type
     * @apiSuccess {number} [size] file size, in bytes
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiError (Error 200 code = 2) {number} code file is not a diretory
     * @apiError (Error 200 code = 3) {number} code file has excessive subitems
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": [
     *         {
     *             "id": "2",
     *             "pid": "1",
     *             "name": "filename",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *             "type": "text/plain",
     *             "size": 1024,
     *         },
     *         {
     *             "id": "3",
     *             "pid": "1",
     *             "name": "folder_name",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *         },
     *     ]
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
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
     * @api {get} /file/(list|ls)/page List a directory by page
     * @apiDescription List a specified directory by page
     * @apiName ListPage
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiParam {string} [last] last `file name`, `null` means first page
     * @apiParam {number} size page size, max value is 1024
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiError (Error 200 code = 2) {number} code file is not a directory
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": [
     *         {
     *             "id": "2",
     *             "pid": "1",
     *             "name": "filename",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *             "type": "text/plain",
     *             "size": 1024,
     *         },
     *         {
     *             "id": "3",
     *             "pid": "1",
     *             "name": "folder_name",
     *             "ctime": 1588166983066,
     *             "mtime": 1588166983066,
     *         },
     *     ]
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/list/page", "/ls/page"})
    public Mono<RestBody<List<FileItemView>>> listPage(
            @Valid @RequestBody Mono<IdPageQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.listPage(it.getId(), it.getLast(), it.getSize(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/(list|ls)/path List a path chain
     * @apiDescription List a path chain
     * @apiName ListPath
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": [
     *         {
     *             "id": "0",
     *             "name": "/",
     *         },
     *         {
     *             "id": "3",
     *             "name": "folder_name",
     *         },
     *         {
     *             "id": "5",
     *             "name": "filename",
     *         }
     *     ]
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/list/path", "/ls/path"})
    public Mono<RestBody<List<IdNameView>>> listPath(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.listPath(it.getId(), exchange));
    }

    /**
     * <pre>
     * @api {get} /file/tree Get a directory tree
     * @apiDescription Get a directory tree, like `tree`
     * @apiName Tree
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiSuccess {string} path file path, root directory is '/'
     * @apiSuccess {string} id file id
     * @apiSuccess {string} pid parent file id
     * @apiSuccess {string} name file base name
     * @apiSuccess {number} ctime created time, in milliseconds
     * @apiSuccess {number} mtime modified time, in milliseconds
     * @apiSuccess {string} [type] file type, in mime type
     * @apiSuccess {number} [size] file size, in bytes
     * @apiSuccess {Array} [items] sub items
     * @apiError (Error 200 code = 1) {number} code file is not found
     * @apiError (Error 200 code = 2) {number} code file is not a directory
     * @apiError (Error 200 code = 3) {number} code file has excessive sub items
     * @apiError (Error 200 code = 4) {number} code dir tree has excessive items
     * @apiParamExample {json} Request-Example:
     * {
     *     "id": "2",
     *     "level": 3
     * }
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *         "id": "2",
     *         "pid": "1",
     *         "path": "/path/to/folder_name",
     *         "name": "folder_name"
     *         "ctime": 1588166983066,
     *         "mtime": 1588166983066,
     *         "items": [
     *             {
     *                 "id": "3",
     *                 "pid": "2",
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
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
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
     * @api {get} /file/flat/tree Get a flat tree directory
     * @apiDescription Get a flat tree directory, `error code` is same as `tree`
     * @apiName FlatTree
     * @apiGroup File
     * @apiParam {string} id file id
     * @apiSuccess {string} id file id
     * @apiSuccess {string} pid parent file id
     * @apiSuccess {string} name file base name
     * @apiSuccess {number} ctime created time, in milliseconds
     * @apiSuccess {number} mtime modified time, in milliseconds
     * @apiSuccess {string} [type] file type, in mime type
     * @apiSuccess {number} [size] file size, in bytes
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *         "/path/to/folder_name": [
     *             {
     *                 "id": "3",
     *                 "pid": "1",
     *                 "name": "filename",
     *                 "ctime": 1588166983066,
     *                 "mtime": 1588166983066,
     *                 "type": "text/plain",
     *                 "size": 1024
     *             }
     *         ]
     *     }
     * }
     * @apiError (Error 403 code = 1) {number} code insufficient permissions
     * </pre>
     */
    @RequestMapping(path = {"/flat/tree"})
    public Mono<RestBody<Map<String, List<FileItemView>>>> listFlatDirTree(
            @Valid @RequestBody Mono<IdQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.flatTree(it.getId(), exchange));
    }

}
