package org.dreamcat.maid.cassandra;

import com.datastax.oss.driver.api.core.CqlIdentifier;
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

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

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
        var statement = selectFrom("user_file")
                .all()
                .whereColumn("uid").isEqualTo(literal(314))
                .whereColumn("id")
                .isGreaterThan(function("\"token\"", literal(271)))
                .limit(100)
                .build();
        var cql = statement.getQuery();
        log.info(cql);

        statement = selectFrom("share_file")
                .all()
                .whereColumn("uid").isEqualTo(literal(314))
                .whereColumn(CqlIdentifier.fromInternal("token(id)"))
                .isGreaterThan(function("\"token\"", literal(271)))
                .limit(100)
                .build();
        cql = statement.getQuery();
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
