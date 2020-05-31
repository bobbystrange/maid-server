package org.dreamcat.maid.api.controller.share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.controller.file.FileInfoView;
import org.dreamcat.maid.api.controller.file.FileItemView;
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
@RequestMapping(path = AppConfig.API_PREFIX + "/share")
public class ShareController {
    private final ShareService service;

    /**
     * @api {post} /share/file File a shared file
     * @apiDescription Get file information by the specified path and shared file id
     * @apiName FileShareFile
     * @apiGroup FileShare
     * @apiParam {number} sid share file id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [path] shared file path, only pass it in share dir case
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code shared file not found
     * @apiError (Error 200 code = 2) code shared file require password
     * @apiError (Error 200 code = 3) code wrong password
     * @apiError (Error 200 code = 4) code shared file is expired
     * @apiError (Error 200 code = 5) code shared file is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) code shared path not found
     * @apiError (Error 200 code = 8) code shared path not dir
     * @apiPermission anonymous
     */
    @RequestMapping(path = "/file", method = RequestMethod.GET)
    public Mono<RestBody<FileInfoView>> file(
            @Valid @RequestBody GetShareFileQuery query) {
        return Mono.fromCallable(() -> service.file(query));
    }

    /**
     * @api {post} /share/list List a shared file
     * @apiDescription List dir by the specified path and shared file id
     * @apiName ListShareFile
     * @apiGroup FileShare
     * @apiParam {number} sid share file id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [path] shared file path, only pass it in share dir case
     * @apiSuccess (Success 200) code 0
     * @apiError (Error 200 code = 1) code shared file not found
     * @apiError (Error 200 code = 2) code shared file require password
     * @apiError (Error 200 code = 3) code wrong password
     * @apiError (Error 200 code = 4) code shared file is expired
     * @apiError (Error 200 code = 5) code shared file is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) code shared path not found
     * @apiError (Error 200 code = 8) code shared path not dir
     * @apiError (Error 200 code = 9) code shared path has excessive sub items, over 1024
     * @apiPermission anonymous
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public Mono<RestBody<List<FileItemView>>> list(
            @Valid @RequestBody GetShareFileQuery query) {
        return Mono.fromCallable(() -> service.list(query));
    }

    /**
     * @api {post} /share/download Download a shared file
     * @apiDescription Download file by the specified path and shared file id
     * @apiName DownloadShareFile
     * @apiGroup FileShare
     * @apiParam {number} sid share file id
     * @apiParam {string} [password] shared file access password
     * @apiParam {string} [path] shared file path, only pass it in share dir case
     * @apiSuccess (Success 200) code 0
     * @apiSuccess (Success 200) {string} data download url
     * @apiError (Error 200 code = 1) code shared file not found
     * @apiError (Error 200 code = 2) code shared file require password
     * @apiError (Error 200 code = 3) code wrong password
     * @apiError (Error 200 code = 4) code shared file is expired
     * @apiError (Error 200 code = 5) code shared file is already invalid since its linked file is moved
     * @apiError (Error 200 code = 6) code shared path not found
     * @apiError (Error 200 code = 7) code shared path not file
     * @apiPermission anonymous
     */
    @RequestMapping(path = "/download", method = RequestMethod.POST)
    public Mono<RestBody<String>> download(
            @Valid @RequestBody GetShareFileQuery query,
            @RequestParam Boolean attachment) {
        return Mono.fromCallable(() -> service.download(query, attachment));
    }

}
