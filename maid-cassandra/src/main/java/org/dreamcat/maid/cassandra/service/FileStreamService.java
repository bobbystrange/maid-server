package org.dreamcat.maid.cassandra.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 * Create by tuke on 2020/3/29
 */
@Slf4j
@Service
public class FileStreamService {

    public Mono<ServerResponse> download(File file, String filename) {
        Resource resource = new FileSystemResource(file);
        return ServerResponse.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(BodyInserters.fromResource(resource));
    }

    public Mono<Void> downloadByWriteWith(
            ServerHttpResponse response, File file, String filename) {
        ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
        ContentDisposition contentDisposition = ContentDisposition.parse(
                "attachment; filename=" + filename);
        response.getHeaders().setContentDisposition(contentDisposition);
        response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return zeroCopyResponse.writeWith(file, 0, file.length());
    }

}
