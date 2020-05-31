package org.dreamcat.maid.cassandra;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.crypto.MD5Util;
import org.dreamcat.common.hc.okhttp.OkHttpWget;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

/**
 * Create by tuke on 2020/4/13
 */
@Slf4j
public class HubTest {

    public static void main(String[] args) throws IOException {
        var properties = new AppProperties();
        var rest = new AppProperties.Rest();
        rest.setSignKey("2C45435E-C680-4B3A-A99B-5319541B24A6");
        properties.setRest(rest);
        var restService = new RestService(properties);

        String url = restService.concatUploadURL("7f9418207fb63284849891cc8a0dea80", "file1.maid.singlar.org");
        var formData = new HashMap<String, Object>();
        formData.put("file", new File("/Users/tuke/data/maid/7f9418207fb63284849891cc8a0dea80"));
        new OkHttpWget().postFormData(url, formData);
    }

    @Test
    public void testQueryBuilder() {
        // SELECT path,uid,ctime,mtime,digest,type,size,count(items) FROM user_file WHERE path=? AND uid=?;
        var cql = QueryBuilder.select()
                .column("path")
                .column("uid")
                .column("ctime")
                .column("mtime")
                .column("digest")
                .column("type")
                .column("size")
                .count("items")
                .from("user_file")
                .where(QueryBuilder.eq("path", "/path/to/folder"))
                .and(QueryBuilder.eq("uid", UUID.randomUUID()))
                .getQueryString();
        log.info(cql);
    }

    @Test
    public void testEmptyFileMD5() throws IOException {
        var p = Files.createTempFile("maid", "test");
        var file = p.toFile();
        // d41d8cd98f00b204e9800998ecf8427e
        var md5 = MD5Util.md5Hex(file);
        log.info("{}, {}, ({})", file.exists(), file.length(), md5);
        Files.deleteIfExists(p);
        assert "d41d8cd98f00b204e9800998ecf8427e".equals(md5);
    }
}
