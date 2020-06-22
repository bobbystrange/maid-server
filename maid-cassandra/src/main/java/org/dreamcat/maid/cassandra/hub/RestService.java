package org.dreamcat.maid.cassandra.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.bean.BeanMapUtil;
import org.dreamcat.common.crypto.SignUtil;
import org.dreamcat.common.util.RandomUtil;
import org.dreamcat.common.util.UrlUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tuke on 2020/4/13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RestService {
    private final AppProperties properties;

    public String concatUploadURL(String digest, String serverAddress) {
        var queryMap = newUploadQueryMap(digest);
        var baseUrl = String.format("http://%s/file/upload", serverAddress);
        return UrlUtil.concatUrl(baseUrl, queryMap);
    }

    public String concatDownloadURL(String digest, String serverAddress, String filename, String type) {
        var queryMap = newDownLoadQueryMap(digest, filename, type, true);
        var baseUrl = String.format("http://%s/file/download", serverAddress);
        return UrlUtil.concatUrl(baseUrl, queryMap);
    }

    public String concatFetchURL(String digest, String serverAddress, String filename, String type) {
        var queryMap = newDownLoadQueryMap(digest, filename, type, false);
        var baseUrl = String.format("http://%s/file/download", serverAddress);
        return UrlUtil.concatUrl(baseUrl, queryMap);
    }

    private Map<String, Object> newUploadQueryMap(String digest) {
        var query = new UploadQuery();
        query.setDigest(digest);
        query.setTimestamp(System.currentTimeMillis());
        query.setNonce(RandomUtil.randi(1 << 16));

        var queryMap = BeanMapUtil.toMap(query);
        var rawStr = UrlUtil.toSortedQueryString(queryMap);
        var sign = SignUtil.hs512Base64(rawStr, properties.getRest().getSignKey());
        queryMap.put("sign", sign);
        return queryMap;
    }

    private Map<String, Object> newDownLoadQueryMap(String digest, String filename, String type, boolean attachment) {
        var query = new DownloadQuery();
        query.setDigest(digest);
        query.setTimestamp(System.currentTimeMillis());
        query.setNonce(RandomUtil.randi(1 << 16));
        query.setFilename(filename);
        query.setType(type);
        query.setAttachment(attachment);

        var queryMap = BeanMapUtil.toMap(query);
        var rawStr = UrlUtil.toSortedQueryString(queryMap);
        var sign = SignUtil.hs512Base64(rawStr, properties.getRest().getSignKey());
        queryMap.put("sign", sign);
        return queryMap;
    }


}
