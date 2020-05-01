package org.dreamcat.maid.cassandra;

import org.dreamcat.common.hc.okhttp.OkHttpUtil;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.cassandra.hub.RestService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Create by tuke on 2020/4/13
 */
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
        OkHttpUtil.postMultipartForm(url, formData);
    }
}
