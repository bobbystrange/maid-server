package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.crypto.MD5Util;
import org.dreamcat.common.hc.okhttp.OkHttpWget;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.RandomUtil;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.controller.file.ShareFileQuery;
import org.dreamcat.maid.api.service.FileLoadService;
import org.dreamcat.maid.api.util.TikaUtil;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.dreamcat.maid.cassandra.entity.ShareFileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;

import static org.dreamcat.maid.cassandra.core.RestCodes.*;

/**
 * Create by tuke on 2020/3/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileLoadServiceImpl implements FileLoadService {
    private final UserFileDao userFileDao;
    private final CassandraTemplate cassandraTemplate;
    private final CommonService commonService;
    private final AppProperties properties;
    private final InstanceService instanceService;
    private final RestService restService;
    private final OkHttpWget wget;
    private final PasswordEncoder passwordEncoder;
    private final IdGeneratorService idGeneratorService;

    @Override
    public RestBody<?> upload(long pid, FilePart filePart, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.findById(pid);
        if (dir == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!dir.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        if (commonService.isFile(dir)) {
            return RestBody.error(fid_not_diretory, "fid is not a diretory");
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
                throw new InternalServerErrorException(sign_or_io_failed, "Sign error");
            }

            var file = new File(properties.getFilePath().getUpload(), sign);
            if (!file.exists()) {
                try {
                    Files.move(tempFile.toPath(), file.toPath());
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new InternalServerErrorException(sign_or_io_failed, "I/O error");
                }
            }
            var size = file.length();
            var fileEntity = new FileEntity();
            fileEntity.setDigest(sign);
            fileEntity.setType(type);
            fileEntity.setSize(size);
            if (!createFile(dir, name, fileEntity)) {
                return RestBody.error(upload_falied, "upload falied");
            }
            return RestBody.ok();
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete file {}", tempFile);
            }
        }

    }

    @Override
    public RestBody<String> download(long fid, boolean attachment, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var file = userFileDao.findById(fid);
        if (file == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!file.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }
        if (commonService.isDirectory(file)) {
            return RestBody.error(fid_not_file, "fid is not a file");
        }

        var digest = file.getDigest();
        var domain = instanceService.findMostIdleDomainAddress(digest);
        if (domain == null) {
            log.error("No available instances for download {}", digest);
            throw new InternalServerErrorException(download_no_available_instances, "no available instances");
        }
        var name = file.getName();
        var type = file.getType();
        String url;
        if (attachment) {
            url = restService.concatDownloadURL(digest, domain, name, type);
        } else {
            url = restService.concatFetchURL(digest, domain, name, type);
        }
        return RestBody.ok(url);
    }

    @Override
    public RestBody<Long> share(ShareFileQuery query, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var fid = query.getId();
        var password = query.getPassword();
        var ttl = query.getTtl();

        var userFile = userFileDao.findById(fid);
        if (userFile == null) {
            return RestBody.error(fid_not_found, "fid not found");
        }
        if (!userFile.getUid().equals(uid)) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        password = passwordEncoder.encode(password);
        var timestamp = System.currentTimeMillis();
        var sid = idGeneratorService.nextSid();

        var shareFile = new ShareFileEntity();
        shareFile.setId(sid);
        shareFile.setFid(fid);
        shareFile.setCtime(timestamp);
        shareFile.setPassword(password);
        shareFile.setTtl(ttl);
        cassandraTemplate.insert(shareFile);
        return RestBody.ok(sid);
    }

    private boolean createFile(UserFileEntity directory, String name, FileEntity fileEntity) {
        var digest = fileEntity.getDigest();

        var result = cassandraTemplate.insert(fileEntity, InsertOptions.builder()
                .withIfNotExists()
                .build());

        boolean error = true;
        var realFile = new File(properties.getFilePath().getUpload(), digest);
        try {
            // already in file table
            if (!result.wasApplied()) {
                var applied = saveUserFile(directory, name, fileEntity);
                if (applied) error = false;
                return applied;
            }

            var address = instanceService.findFreestAddress();
            if (address == null) {
                log.error("No available instances for upload {}", digest);
                throw new InternalServerErrorException(upload_no_available_instances, "no available instances");
            }
            var domain = instanceService.mapAddressToDomain(address);
            String url = restService.concatUploadURL(digest, domain);
            var formData = new HashMap<String, Object>();
            formData.put("file", realFile);
            try {
                log.info("Uploading file {} to {} via post {}", digest, domain, url);
                var res = wget.postFormData(url, formData);
                if (res.isSuccessful()) {
                    var applied = saveUserFile(directory, name, fileEntity);
                    if (applied) error = false;
                    return applied;
                } else {
                    log.error("{} {} on upload file {} to {}", res.code(), res.message(), digest, domain);
                    throw new InternalServerErrorException(upload_save_hub_failed, "upload save hub failed");
                }
            } catch (IOException e) {
                log.error("Error on upload file {} to {}, caused by `{}`", digest, domain, e.getMessage());
                throw new InternalServerErrorException(upload_save_hub_failed, "upload save hub failed");
            }
        } finally {
            if (realFile.exists() && !realFile.delete()) {
                log.warn("Failed to delete file {}", realFile.getAbsolutePath());
            }

            if (error) {
                cassandraTemplate.delete(fileEntity);
            }
        }
    }

    // return false means directory maybe deleted
    private boolean saveUserFile(UserFileEntity directory, String name, FileEntity fileEntity) {
        if (trySaveUserFile(directory, name, fileEntity)) return true;

        name = String.format("%s-%s.%s",
                FileUtil.prefix(name), RandomUtil.uuid32(), FileUtil.suffix(name));
        return trySaveUserFile(directory, name, fileEntity);
    }

    private boolean trySaveUserFile(UserFileEntity directory, String name, FileEntity fileEntity) {
        var uid = directory.getUid();
        var pid = directory.getId();

        var file = commonService.newUserFile(uid, pid, name);
        BeanCopierUtil.copy(fileEntity, file);

        directory.setMtime(file.getMtime());

        return cassandraTemplate.batchOps()
                .update(directory)
                .insert(Collections.singleton(file), InsertOptions.builder()
                        .withIfNotExists()
                        .build())
                .execute()
                .wasApplied();
    }

}
