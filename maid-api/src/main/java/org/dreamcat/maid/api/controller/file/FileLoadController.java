package org.dreamcat.maid.api.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.PathQuery;
import org.dreamcat.maid.api.service.FileLoadService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/file")
public class FileLoadController {
    private final FileLoadService service;

    @RequestMapping(path = "/upload", method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<RestBody<?>> uploadFile(
            @RequestParam String path,
            @RequestPart("file") FilePart filePart,
            ServerWebExchange exchange) {
        //AsynchronousFileChannel channel =
        //        AsynchronousFileChannel.open((Path) null, StandardOpenOption.WRITE);
        //DataBufferUtils.write(((FilePart) null).content(), channel, 0)
        //        .doOnComplete(() -> {
        //            System.out.println("finish");
        //        })
        //        .subscribe();
        return Mono.fromCallable(() -> service.uploadFile(path, filePart, exchange));
    }

    @RequestMapping(path = "/download", method = RequestMethod.GET)
    public Mono<RestBody<String>> downloadFile(
            @RequestParam String path,
            @RequestParam(required = false) Boolean asAttachment,
            ServerWebExchange exchange) {
        if (!PathQuery.PATTERN_PATH_EXCLUDE_ROOT.matcher(path).matches()) {
            throw new BadRequestException("Invalid path");
        }

        var attachment = asAttachment == null || asAttachment;
        return Mono.fromCallable(() -> service.downloadFile(path, attachment, exchange));
    }

}
