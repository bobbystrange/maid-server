package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.hc.okhttp.OkHttpUtil;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.entity.FileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Create by tuke on 2020/4/14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileBuildService {
    private final AppProperties properties;
    private final CommonService commonService;
    private final CassandraTemplate cassandraTemplate;
    private final FileChainService fileChainService;
    // maid-hub
    private final InstanceService instanceService;
    private final RestService restService;

    public void createFile(UserFileEntity directory, String name, UUID uid, String digest, String type, long size) {
        var fileEntity = new FileEntity();
        fileEntity.setDigest(digest);
        fileEntity.setType(type);
        fileEntity.setSize(size);
        var result = cassandraTemplate.insert(fileEntity, InsertOptions.builder()
                .ifNotExists(true)
                .build());

        boolean error = true;
        var realFile = new File(properties.getFilePath().getUpload(), digest);
        try {
            // already in file table
            if (!result.wasApplied()) {
                saveUserFile(directory, name, uid, digest, type, size);
                return;
            }

            var address = instanceService.findFreestAddress();
            if (address == null) {
                log.error("No freest instance for upload {}", digest);
                throw new InternalServerErrorException("No available server to save file in hub");
            }
            var domain = instanceService.mapAddressToDomain(address);
            String url = restService.concatUploadURL(digest, domain);
            var formData = new HashMap<String, Object>();
            formData.put("file", realFile);
            try {
                log.info("Uploading file {} to {} via post {}", digest, domain, url);
                OkHttpUtil.postMultipartForm(url, formData);
                saveUserFile(directory, name, uid, digest, type, size);
                error = false;
            } catch (IOException e) {
                log.error("Error on upload file {} to {}, caused by `{}`", digest, domain, e.getMessage());
                throw new InternalServerErrorException("Failed to save file to hub");
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

    private void saveUserFile(
            UserFileEntity directory, String name, UUID uid,
            String digest, String type, long size) {
        var file = new UserFileEntity();
        var timestamp = System.currentTimeMillis();
        var path = directory.getPath() + "/" + name;
        path = FileUtil.normalize(path);
        commonService.fillEntity(file, timestamp, uid, path);
        file.setDigest(digest);
        file.setType(type);
        file.setSize(size);
        fileChainService.associate(directory, file);

        // keep consistency
        cassandraTemplate.batchOps()
                // Batch with conditions cannot span multiple tables
                .insert(directory, file)
                .execute();
    }
}
