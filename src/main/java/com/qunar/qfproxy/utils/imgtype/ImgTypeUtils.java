package com.qunar.qfproxy.utils.imgtype;


import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


public class ImgTypeUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ImgTypeUtils.class);

    private static final Map<int[], String> IMG_TYPES = new LinkedHashMap<>();

    static {
        IMG_TYPES.put(new int[]{0xff, 0xd8, 0xff, 0xe1}, "jpg");
        IMG_TYPES.put(new int[]{0xff, 0xd8, 0xff, 0xe0}, "jpg");
        IMG_TYPES.put(new int[]{0xff, 0xd8, 0xff, 0xdb}, "jpg");
        IMG_TYPES.put(new int[]{0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}, "png");
        IMG_TYPES.put(new int[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61}, "gif");
        IMG_TYPES.put(new int[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, "gif");
        IMG_TYPES.put(new int[]{0x42, 0x4d}, "bmp");
        IMG_TYPES.put(new int[]{0x38, 0x42, 0x50, 0x53}, "psd");
        IMG_TYPES.put(new int[]{0x4d, 0x4d, 0x00, 0x2a}, "tiff");
        IMG_TYPES.put(new int[]{0x49, 0x49, 0x2a, 0x00}, "tiff");
        IMG_TYPES.put(new int[]{0x46, 0x4f, 0x52, 0x4d}, "iff");
        IMG_TYPES.put(new int[]{0x00, 0x00, 0x01, 0x00}, "ico");
        IMG_TYPES.put(new int[]{0x52, 0x49, 0x46, 0x46, -0x01, -0x01, -0x01, -0x01, 0x57, 0x45, 0x42, 0x50}, "webp");
    }


    /**
     * 检测是不是已知格式的图片
     *
     * @param imgBytes byte数组，可以很长，因为无论多长都最多只需12个字节，如果还有图片格式的特征长度超过12，那么这个取的长度也要相应增加
     * @return 如果检测到，就返回图片的类型
     */
    public static String detect(byte[] imgBytes) {
        if (imgBytes == null || imgBytes.length == 0) {
            return null;
        }
        byte[] imgHeader;
        if (imgBytes.length > 12) {
            imgHeader = Arrays.copyOfRange(imgBytes, 0, 12);
        } else {
            imgHeader = imgBytes;
        }

        int headerLength = imgHeader.length;
        boolean flag = false;
        for (Map.Entry<int[], String> entry : IMG_TYPES.entrySet()) {
            int[] imgTypeInts = entry.getKey();
            for (int i = 0; i < imgTypeInts.length; i++) {
                if (i >= headerLength) {
                    flag = false;
                    break;
                }

                flag = imgTypeInts[i] < 0 || imgTypeInts[i] == (imgHeader[i] & 0xff);
                if (!flag) {
                    break;
                }
            }
            if (flag) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static String detect(InputStreamWrapper inputStreamWrapper) {
        byte[] buffer = inputStreamWrapper.getBuffer();
        return detect(buffer);
    }
}
