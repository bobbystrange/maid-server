package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.crypto.MD5Util;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.controller.file.CreateFileQuery;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.controller.file.UpdateFileQuery;
import org.dreamcat.maid.api.service.FileService;
import org.dreamcat.maid.api.util.TikaUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Create by tuke on 2020/3/18
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {
    private final UserFileDao fileDao;
    private final CommonService commonService;
    private final FileChainService fileChainService;
    private final AppProperties properties;
    private final FileBuildService fileBuildService;

    @Override
    public RestBody<String> createFile(CreateFileQuery query, ServerWebExchange exchange) {
        String path = query.getPath();
        String content = query.getContent();
        String name = query.getName();

        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity directory = fileDao.find(uid, path);
        if (directory == null) {
            return RestBody.error("Directory %s doesn't exist", path);
        }
        if (commonService.isFile(directory)) {
            return RestBody.error("%s is not a diretory", path);
        }

        String tempName = UUID.randomUUID().toString() + "." + FileUtil.basename(path);
        File tempFile = new File(properties.getFilePath().getTempUpdate(), tempName);
        try {
            FileUtil.writeFrom(tempFile, content);
            String sign;
            String type;
            try {
                sign = MD5Util.md5Hex(tempFile);
                type = TikaUtil.detect(tempFile);
            } catch (IOException e) {
                log.error(e.getMessage());
                return RestBody.error("Signature error on %s", path);
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
        } catch (IOException e) {
            log.error(e.getMessage());
            return RestBody.error("Flush content to disk error on %s", path);
        } finally {
            if (!tempFile.delete()) {
                log.warn("Failed to delete file {}", tempFile);
            }
        }
        return RestBody.ok();
    }

    @Override
    public RestBody<?> deleteFile(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity entity = fileDao.find(uid, path);
        if (entity == null) {
            return RestBody.error("File %s doesn't exist", path);
        }
        fileChainService.delete(entity, uid);
        return RestBody.ok();
    }

    @Override
    public RestBody<FileView> getFile(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity entity = fileDao.find(uid, path);
        if (entity == null) {
            return RestBody.error("File %s doesn't exist", path);
        }
        FileView view = BeanCopierUtil.copy(entity, FileView.class);
        view.setName(FileUtil.basename(entity.getPath()));
        return RestBody.ok(view);
    }

    @Override
    public RestBody<?> updateFile(UpdateFileQuery query, ServerWebExchange exchange) {
        String path = query.getPath();
        String content = query.getContent();

        commonService.checkPath(path);
        UUID uid = commonService.retrieveUid(exchange);
        UserFileEntity entity = fileDao.find(uid, path);
        if (entity == null) {
            return RestBody.error("File %s doesn't exist", path);
        }

        String name = FileUtil.basename(path);
        String tempName = UUID.randomUUID().toString() + "." + FileUtil.basename(path);
        File tempFile = new File(properties.getFilePath().getTempUpdate(), tempName);
        try {
            FileUtil.writeFrom(tempFile, content);
            String sign;
            String type;
            try {
                sign = MD5Util.md5Hex(tempFile);
                type = TikaUtil.detect(tempFile);
            } catch (IOException e) {
                log.error(e.getMessage());
                return RestBody.error("Signature error on %s", path);
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

            fileChainService.updateFileContent(entity, sign, type);
        } catch (IOException e) {
            log.error(e.getMessage());
            return RestBody.error("Flush content to disk error on %s", path);
        } finally {
            if (!tempFile.delete()) {
                log.warn("Failed to delete file {}", tempFile);
            }
        }
        return RestBody.ok();
    }

}
