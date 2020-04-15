package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.crypto.MD5Util;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.service.FileLoadService;
import org.dreamcat.maid.api.util.TikaUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileLoadServiceImpl implements FileLoadService {
    private final UserFileDao fileDao;
    private final FileBuildService fileBuildService;
    private final CommonService commonService;
    private final AppProperties properties;
    // maid-hub
    private final InstanceService instanceService;
    private final RestService restService;


    @Override
    public RestBody<?> uploadFile(String directoryPath, FilePart filePart, ServerWebExchange exchange) {
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity directory = fileDao.find(uid, directoryPath);
        if (directory == null) {
            return RestBody.error("Directory %s doesn't exist", directoryPath);
        }
        if (commonService.isFile(directory)) {
            return RestBody.error("%s is not a diretory", directoryPath);
        }

        String name = filePart.filename();
        File tempFile = new File(properties.getFilePath().getTempUpload(), name);
        try {
            filePart.transferTo(tempFile);
            String sign;
            String type;
            try {
                sign = MD5Util.md5Hex(tempFile);
                type = TikaUtil.detect(tempFile);
            } catch (IOException e) {
                log.error(e.getMessage());
                return RestBody.error("Sign error on %s", name);
            }

            File file = new File(properties.getFilePath().getUpload(), sign);
            if (!file.exists()) {
                try {
                    Files.move(tempFile.toPath(), file.toPath());
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return RestBody.error("Save error on %s", name);
                }
            }
            long size = file.length();
            fileBuildService.createFile(directory, name, uid, sign, type, size);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete file {}", tempFile);
            }
        }

        return RestBody.ok();
    }

    @Override
    public RestBody<String> downloadFile(String path, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = fileDao.find(uid, path);
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
            throw new NotFoundException();
        }

        var basename = FileUtil.basename(file.getPath());
        var type = file.getType();
        var url = restService.concatDownloadURL(digest, domain, basename, type);
        return RestBody.ok(url);
    }

    @Override
    public RestBody<String> shareFile(String id) {
        return null;
    }


}
