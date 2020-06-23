package org.dreamcat.maid.api.controller.share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.IdNameView;
import org.dreamcat.maid.api.service.ShareService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

/**
 * Create by tuke on 2020/5/24
 *
 * @apiDefine anonymous Which doesn't require the JWT Authorization
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/share",
        method = RequestMethod.POST)
public class ShareController {
    private final ShareService service;

    /**
     * @api {post} /share/file Get file information
     * @apiDescription Get file information by share id and file id
     * @apiName File
     * @apiGroup Share
     * @apiParam {string} sid share id
     * @apiParam {string} [password] share access password
     * @apiParam {string} [fid] shared sub file id, only pass it in share dir case
     * @apiSuccess {object} data it has two more fields: ttl, stime than `/file/file`
     * @apiSuccess {number} ttl time-to-live in second
     * @apiSuccess {number} stime shared time, the deadline of the share id is `stime + ttl * 1000` milliseconds
     * @apiError (Error 200 code = 1) {number} code share not found
     * @apiError (Error 200 code = 2) {number} code share require password
     * @apiError (Error 200 code = 3) {number} code wrong password
     * @apiError (Error 200 code = 4) {number} code share is expired
     * @apiError (Error 200 code = 5) {number} code shared is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) {number} code file is not found
     * @apiError (Error 200 code = 7) {number} code file is not shared
     * @apiError (Error 200 code = 8) {number} code file is not a dir
     * @apiPermission anonymous
     */
    @RequestMapping(path = "/file")
    public Mono<RestBody<ShareFileInfoView>> file(
            @Valid @RequestBody GetShareFileQuery query) {
        return Mono.fromCallable(() -> service.file(query));
    }

    /**
     * @api {post} /share/list List a shared file
     * @apiDescription List dir by the specified file id and share id
     * @apiName ListFile
     * @apiGroup Share
     * @apiParam {string} sid share id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [fid] shared sub file id, only pass it in share dir case
     * @apiSuccess {Array} data same as `/file/list`
     * @apiError (Error 200 code = 1) {number} code share not found
     * @apiError (Error 200 code = 2) {number} code share require password
     * @apiError (Error 200 code = 3) {number} code wrong password
     * @apiError (Error 200 code = 4) {number} code share is expired
     * @apiError (Error 200 code = 5) {number} code share is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) {number} code file is not found
     * @apiError (Error 200 code = 7) {number} code file is not shared
     * @apiError (Error 200 code = 8) {number} code file is not a dir
     * @apiError (Error 200 code = 9) {number} code file has excessive subitems
     * @apiPermission anonymous
     */
    @RequestMapping(path = {"/list", "ls"})
    public Mono<RestBody<List<FileItemView>>> list(
            @Valid @RequestBody GetShareFileQuery query) {
        return Mono.fromCallable(() -> service.list(query));
    }

    /**
     * @api {post} /share/list/path List a path chain
     * @apiDescription List a path chain by share id and file id
     * @apiName ListPath
     * @apiGroup Share
     * @apiParam {number} sid share id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [fid] shared sub file id, only pass it in share dir case
     * @apiSuccess {Array} data same as `/file/list/path`
     * @apiError (Error 200 code = 1) {number} code share not found
     * @apiError (Error 200 code = 2) {number} code share require password
     * @apiError (Error 200 code = 3) {number} code wrong password
     * @apiError (Error 200 code = 4) {number} code share is expired
     * @apiError (Error 200 code = 5) {number}code share is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) {number} code file is not found
     * @apiError (Error 200 code = 7) {number} code file is not shared
     * @apiError (Error 200 code = 8) {number} code file is not a dir
     * @apiError (Error 200 code = 9) {number} code file has excessive subitems
     * @apiPermission anonymous
     */
    @RequestMapping(path = {"/list/path", "/ls/path"})
    public Mono<RestBody<List<IdNameView>>> listPath(
            @Valid @RequestBody GetShareFileQuery query) {
        return Mono.fromCallable(() -> service.listPath(query));
    }

    /**
     * @api {post} /share/download Download a shared file
     * @apiDescription Download a shared file
     * @apiName Download
     * @apiGroup Share
     * @apiParam {number} sid share id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [fid] shared sub file id, only pass it in share dir case
     * @apiSuccess {string} data download url
     * @apiError (Error 200 code = 1) {number} code share not found
     * @apiError (Error 200 code = 2) {number} code share require password
     * @apiError (Error 200 code = 3) {number} code wrong password
     * @apiError (Error 200 code = 4) {number} code share is expired
     * @apiError (Error 200 code = 5) {number} code share is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) {number} code file is not found
     * @apiError (Error 200 code = 7) {number} code file is not shared
     * @apiError (Error 200 code = 8) {number} code file is not a file
     * @apiPermission anonymous
     */
    @RequestMapping(path = "/download")
    public Mono<RestBody<String>> download(
            @Valid @RequestBody GetShareFileQuery query,
            @RequestParam Boolean attachment) {
        return Mono.fromCallable(() -> service.download(query, attachment));
    }

}
