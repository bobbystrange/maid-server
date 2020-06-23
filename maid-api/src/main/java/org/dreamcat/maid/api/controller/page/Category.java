package org.dreamcat.maid.api.controller.page;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.dreamcat.common.util.StringUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Create by tuke on 2020/6/17
 */
public enum Category {
    Image,
    Document,
    Video,
    Audio,
    Torrent,
    Other,
    UNKNOWN;

    // http://svn.apache.org/repos/asf/httpd/httpd/trunk/docs/conf/mime.types
    private static final List<String> extra_document_list = Arrays.asList(
            "application/pdf", "application/vnd\\.ms-powerpoint",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd\\.ms-excel",
            "application/x-javascript",
            "application/x-csh", "application/x-sh"
    );

    public boolean belongs(String type) {
        return this.equals(parse(type));
    }

    public Category parse(String type) {
        if (type == null) return Other;

        if (type.matches("^image/.*$")) {
            return Image;
        } else if (type.matches("^audio/.*$")) {
            return Audio;
        } else if (type.matches("^video/.*$")) {
            return Video;
        } else if (type.matches("^text/.*$") ||
                extra_document_list.contains(type)) {
            return Document;
        } else if (type.equals("application/x-bittorrent")) {
            return Torrent;
        } else {
            return Other;
        }
    }

    public static class CategoryDeserializer extends JsonDeserializer<Category> {
        @Override
        public Category deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            var name = p.getValueAsString();
            if (name == null) return Category.UNKNOWN;

            name = StringUtil.toCapital(name.toLowerCase());
            try {
                return Category.valueOf(name);
            } catch (IllegalArgumentException e) {
                return Category.UNKNOWN;
            }
        }
    }

}
