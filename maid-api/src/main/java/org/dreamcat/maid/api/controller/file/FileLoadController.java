package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.service.FileLoadService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file", method = RequestMethod.POST)
public class FileLoadController {
    private final FileLoadService service;

    /**
     * @api {post} /file/upload Upload file
     * @apiDescription Upload a file to specified directory
     * @apiName UploadFile
     * @apiGroup FileLoad
     * @apiParam {number} id file id, in `query parameter`
     * @apiParam {File} file in `multipart/form-data`
     * @apiSuccess (Success 200 code = 0) {Number} code
     * @apiError (Error 200 code = 1) code file is not found
     * @apiError (Error 200 code = 3) code file is not a diretory
     * @apiError (Error 200 code = 4) code upload failed, the diretory maybe deleted during uploading
     * @apiError (Error 403 code = 1) code insufficient permissions
     * @apiError (Error 500 code = 1) code maybe internal I/O error or cannot sign uploaded file
     * @apiError (Error 500 code = 2) code no available instances for upload
     * @apiError (Error 500 code = 3) code failed to upload file to instances
     */
    @RequestMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<RestBody<?>> uploadFile(
            @RequestParam Long id,
            @RequestPart("file") FilePart filePart,
            ServerWebExchange exchange) {
        //AsynchronousFileChannel channel =
        //        AsynchronousFileChannel.open((Path) null, StandardOpenOption.WRITE);
        //DataBufferUtils.write(((FilePart) null).content(), channel, 0)
        //        .doOnComplete(() -> {
        //            System.out.println("finish");
        //        })
        //        .subscribe();
        return Mono.fromCallable(() ->
                service.upload(id, filePart, exchange));
    }

    /**
     * @api {post} /file/download Download file
     * @apiDescription Download a file by file id
     * @apiName DownloadFile
     * @apiGroup FileLoad
     * @apiParam {number} id file id, in `query parameter`
     * @apiParam {boolean} [attachment] true to add download header, in `query parameter`
     * @apiSuccess (Success 200) {number} code 0
     * @apiSuccess (Success 200) {string} data download url
     * @apiError (Error 200 code = 1) code file is not found
     * @apiError (Error 200 code = 2) code file is not a file
     * @apiError (Error 500 code = 1) code no available instances for download
     * @apiSuccessExample {json} Success-Response:
     * {
     * "code": 0,
     * "data": "http://..."
     * }
     * @apiError (Error 403 code = 1) code insufficient permissions
     */
    @RequestMapping(path = "/download")
    public Mono<RestBody<String>> downloadFile(
            @RequestParam Long id,
            @RequestParam(required = false) boolean attachment,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> service.download(id, attachment, exchange));
    }

    /**
     * @api {post} /file/share Share file or directory
     * @apiDescription Share a file or directory by file path
     * @apiName ShareFile
     * @apiGroup FileLoad
     * @apiParam {number} id file id
     * @apiParam {string} [password] shared file access password
     * @apiParam {number} [ttl] shared file time-to-live
     * @apiSuccess (Success 200) {Number} code 0
     * @apiSuccess (Success 200) {String} data share code
     * @apiError (Error 404) {Number} code -1, path or file-instance is not found
     */
    @RequestMapping(path = "/share")
    public Mono<RestBody<Long>> shareFile(
            @Valid @RequestBody ShareFileQuery query,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> service.share(query, exchange));
    }


}
