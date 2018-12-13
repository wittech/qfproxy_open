package com.qunar.qfproxy.controller;

import com.qunar.qfproxy.constants.StorageConfig;
import com.qunar.qfproxy.model.FileType;
import com.qunar.qfproxy.model.JsonResult;
import com.qunar.qfproxy.utils.DownloadUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import static com.qunar.qfproxy.constants.CodeConstants.*;
import static com.qunar.qfproxy.utils.ErrorCodeUtil.checkParamsAndCode;

@Controller
@RequestMapping("/")
public class UploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    @RequestMapping("/{ver:v[0-9]+}/inspection/{type}")
    @ResponseBody
    public JsonResult inspect(@PathVariable(value = "ver") String ver,
                              @PathVariable(value = "type") String type,
                              @RequestParam(value = "key") String key,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "p", required = false) String platform,
                              @RequestParam(value = "v", required = false) String clientVer,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        FileType fileType = FileType.of(type);

        boolean checkRes = checkParamsAndCode(response, key, ILLEGAL_KEY)
                && checkParamsAndCode(response, fileType, ILLEGAL_TYPE);
        if (!checkRes) {
            LOGGER.error("检查文件是否存在错误，非法参数, ver:{},type:{},key:{},name:{},p:{},v:{}", ver, type, key, name);
            return JsonResult.newFailResult("非法参数");
        }

        key = handleNameAndKey(key, name, fileType);
        if (StringUtils.isEmpty(name)) {
            name = key;
        }
        File file = new File(StorageConfig.SWIFT_FOLDER + key);
        if (!file.exists()) {
            response.addIntHeader(X_QFPROXY_CODE, FILE_EXIST);
            return JsonResult.newSuccJsonResult("文件不存在", null);
        } else {
            String downloadUri = DownloadUtils.getDownloadUri("v2", key, name);
            return JsonResult.newFailResult("文件已存在", downloadUri);
        }
    }


    private String handleNameAndKey(String key, String name, FileType fileType) {
        if (FileType.FILE.equals(fileType)) {
            return key;
        }

        int indexKey = key.lastIndexOf(".");
        if (indexKey > -1) {
            return key;
        }

        int indexName = StringUtils.isNotEmpty(name) ? name.lastIndexOf(".") : -1;

        if (indexName > -1) {
            key = StringUtils.join(key, name.substring(indexName, name.length()));
        }

        return key;
    }

}
