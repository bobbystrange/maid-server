package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.crypto.MD5Util;
import org.dreamcat.common.function.QuaternaryOperator;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.service.FileLoadService;
import org.dreamcat.maid.api.util.TikaUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileLoadServiceImpl implements FileLoadService {
    private final UserFileDao userFileDao;
    private final FileBuildService fileBuildService;
    private final FileChainService fileChainService;
    private final CommonService commonService;
    private final AppProperties properties;
    // maid-hub
    private final InstanceService instanceService;
    private final RestService restService;

    @Override
    public RestBody<?> uploadFile(String path, FilePart filePart, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var directory = userFileDao.find(uid, path);
        if (directory == null) {
            throw new NotFoundException("diretory " + path + " doesn't exist");
        } else if (commonService.isFile(directory)) {
            throw new ForbiddenException(path + " is not a diretory");
        }

        var name = filePart.filename();
        var tempFile = new File(properties.getFilePath().getTempUpload(), name);
        try {
            filePart.transferTo(tempFile);
            String sign;
            String type;
            try {
                sign = MD5Util.md5Hex(tempFile);
                type = TikaUtil.detect(tempFile);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new InternalServerErrorException("Sign error on " + name);
            }

            var file = new File(properties.getFilePath().getUpload(), sign);
            if (!file.exists()) {
                try {
                    Files.move(tempFile.toPath(), file.toPath());
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new InternalServerErrorException("Save error on " + name);
                }
            }
            var size = file.length();
            fileBuildService.createFile(directory, name, uid, sign, type, size);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete file {}", tempFile);
            }
        }

        return RestBody.ok();
    }

    @Override
    public RestBody<String> downloadFile(String path, boolean asAttachment, ServerWebExchange exchange) {
        if (asAttachment) {
            return findFileURL(path, exchange, restService::concatDownloadURL);
        } else {
            return findFileURL(path, exchange, restService::concatFetchURL);
        }
    }

    private RestBody<String> findFileURL(String path, ServerWebExchange exchange, QuaternaryOperator<String> op) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.find(uid, path);
        if (file == null) {
            throw new NotFoundException("File " + path + " doesn't exist");
        }
        if (commonService.isDirectory(file)) {
            throw new BadRequestException(path + " is not a file");
        }

        var digest = file.getDigest();
        var domain = instanceService.findMostIdleDomainAddress(digest);
        if (domain == null) {
            log.error("No most idle instance contains for download {}", digest);
            throw new InternalServerErrorException("No available instances");
        }

        var basename = FileUtil.basename(file.getPath());
        var type = file.getType();

        var url = op.apply(digest, domain, basename, type);
        return RestBody.ok(url);
    }

    @Override
    public RestBody<String> shareFile(String id) {
        return null;
    }


}
