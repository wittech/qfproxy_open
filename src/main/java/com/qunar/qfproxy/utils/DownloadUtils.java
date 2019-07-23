package com.qunar.qfproxy.utils;

import com.google.common.base.Strings;
import com.qunar.qfproxy.constants.Config;
import com.qunar.qfproxy.constants.HttpConstants;
import com.qunar.qfproxy.model.FileType;
import com.qunar.qfproxy.utils.imgtype.ImgTypeUtils;
import com.qunar.qfproxy.utils.imgtype.InputStreamWrapper;
import org.apache.commons.lang3.StringUtils;


public class DownloadUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtils.class);


    public static String getDownloadUri(String ver, String key, String name) {
        String sufficnt = Config.PROJECT_HOST_AND_PORT;
        if (Strings.isNullOrEmpty(sufficnt)) {
            sufficnt = "";
        }
        return String.format(StringUtils.join(sufficnt, HttpConstants.DOWNLOAD_PATH_FORMAT, "?name=%s"),
                ver, HttpUtils.urlEncode(key), StringUtils.isEmpty(name) ? "" : HttpUtils.urlEncode(name));
    }

    /**
     * 检查文件是不是已知格式的图片，如果是则返回图片类型
     *
     * @param fileType    文件类型
     * @param contentType contentType
     * @param isw         输入流的包装器
     * @return 图片类型
     * @see InputStreamWrapper
     */
    public static String handleImgRealType(FileType fileType, String contentType, InputStreamWrapper isw) {
        if ((StringUtils.containsIgnoreCase(contentType, "image") || FileType.IMG.equals(fileType) || FileType.AVATAR.equals(fileType)) && isw != null) {
            return ImgTypeUtils.detect(isw);
        } else {
            return null;
        }
    }

    /**
     * 为文件添加后缀名
     *
     * @param key      key
     * @param realType 后缀名
     * @return 添加后缀名的key
     */
    public static String handleKeyForImg(String key, String realType) {
        if (StringUtils.isNotEmpty(realType)) {
            return StringUtils.join(key, ".", realType);
        } else {
            return key;
        }
    }

    public static String handleImgName(String name, String imgRealType, String key) {
        if (StringUtils.isNotEmpty(imgRealType) && !key.endsWith(StringUtils.join(".", imgRealType))) {
            return StringUtils.join(key, ".", imgRealType);
        } else {
            return name;
        }
    }
}
