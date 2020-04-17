//package org.dreamcat.maid.cassandra.util;
//
//import net.coobird.thumbnailator.Thumbnails;
//import org.dreamcat.common.io.ImageUtil;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * Create by tuke on 2020/4/16
// */
//public class ThumbUtil {
//    private static final int MAX_DIAMETER = 100;
//
//    public static String thumb(File imageFile) throws IOException {
//        var image = Thumbnails.of(imageFile)
//                //.size(100, 100)
//                .asBufferedImage();
//
//        var dia = Math.max(image.getWidth(), image.getHeight());
//        if (dia > MAX_DIAMETER) {
//            image = Thumbnails.of(image)
//                    .size(MAX_DIAMETER, MAX_DIAMETER)
//                    .asBufferedImage();
//        }
//
//        return ImageUtil.base64ImageSource(image);
//    }
//}
