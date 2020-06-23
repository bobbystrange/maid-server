package org.dreamcat.maid.api.controller.page;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.core.LastPageQuery;
import org.dreamcat.maid.api.core.LastPageView;
import org.dreamcat.maid.api.service.FilePageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/6/16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file/page",
        method = RequestMethod.POST,
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class FilePageController {
    private final FilePageService service;

    /**
     * <pre>
     * @api {post} /file/page/category Get category file list
     * @apiDescription Get category file list by page
     * @apiName Category
     * @apiGroup FilePage
     * @apiParam {string} last last file id in previous page, use it to locate next page you wanna get
     * @apiParam {number} size page size, max value is 1024
     * @apiParam {string} category file category, one of `image, document, video, audio, torrent, other`
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *          "items": [
     *              {
     *                  "id": "3",
     *                  "pid": "1",
     *                  "name": "filename",
     *                  "ctime": 1588166983066,
     *                  "mtime": 1588166983066,
     *                  "type": "text/plain",
     *                  "size": 1024
     *              },
     *          ],
     *          "last": "6",
     *          "hasMore": true
     *     }
     * }
     * @apiSuccess (Success 200) {Array} items file list in current page, the structure of item is same as `/file/list`
     * @apiSuccess (Success 200) {string} last last file id in current page, it might not equal to `items[items.length - 1].id`, use it to request next page
     * @apiSuccess (Success 200) {boolean} hasMore has more or not
     * </pre>
     */
    @RequestMapping(path = {"/category"})
    public Mono<RestBody<LastPageView<FileItemView>>> category(
            @Valid @RequestBody Mono<CategoryQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.category(it, exchange));
    }

    /**
     * <pre>
     * @api {post} /file/page/share Get shared file
     * @apiDescription Get shared file list by page
     * @apiName Share
     * @apiGroup FilePage
     * @apiParam {string} last last file id in previous page, use it to locate next page you wanna get
     * @apiParam {number} size page size, max value is 1024
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "code": 0,
     *     "data": {
     *          "items": [
     *              {
     *                  "id": "655365",
     *                  "ctime": 1588166983066,
     *                  "password": "1314",
     *                  "ttl": 86400,
     *                  "fid": "3"
     *                  "name": "filename",
     *                  "type": "text/plain",
     *                  "size": 1024
     *              },
     *          ],
     *          "last": "6",
     *          "hasMore": true
     *     }
     * }
     * @apiSuccess (Success 200) {Array} items file list in current page
     * @apiSuccess (Success 200) {string} last last file id in current page, it might not equal to `items[items.length - 1].id`, use it to request next page
     * @apiSuccess (Success 200) {boolean} hasMore has more or not
     * </pre>
     */
    @RequestMapping(path = {"/share"})
    public Mono<RestBody<LastPageView<ShareItemView>>> share(
            @Valid @RequestBody Mono<LastPageQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> service.share(it, exchange));
    }

}
