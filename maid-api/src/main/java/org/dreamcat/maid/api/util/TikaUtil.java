package org.dreamcat.maid.api.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

/**
 * Create by tuke on 2020/3/29
 */
@Slf4j
public class TikaUtil {

    private static final Tika TIKA = new Tika();

    public static String detect(File file) {
        try {
            return TIKA.detect(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static boolean isBinary(String type) {
        return !isText(type);
    }

    public static boolean isText(String type) {
        if (type == null) return false;

        return type.startsWith("text/")
                || type.equals("application/xml")
                || type.equals("application/json");

    }
}
