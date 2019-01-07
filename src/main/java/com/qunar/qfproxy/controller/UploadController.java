package com.qunar.qfproxy.controller;

import com.qunar.qfproxy.constants.StorageConfig;
import com.qunar.qfproxy.model.FileType;
import com.qunar.qfproxy.model.JsonResult;
import com.qunar.qfproxy.utils.DownloadUtils;
import com.qunar.qfproxy.utils.imgtype.InputStreamWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import static com.qunar.qfproxy.constants.CodeConstants.*;
import static com.qunar.qfproxy.utils.ErrorCodeUtil.catchExceptionAndSet;
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
  @RequestMapping(value = "/v2/share/upload/{type}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<?> uploadShare(@RequestParam("name") String key,
//                                     @RequestParam("size") String size,
                                     @PathVariable(value = "type") String type,
                                     @RequestParam(value = "file") MultipartFile file,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        return upload(key, key, type, null, null, file, request, response);

    }


    public JsonResult<?> upload(@RequestParam(value = "key") String key,
                                @RequestParam(value = "name", required = false) String name,
                                @PathVariable(value = "type") String type,
                                @RequestParam(value = "p", required = false) String platform,
                                @RequestParam(value = "v", required = false) String clientVer,
                                @RequestParam(value = "file") MultipartFile file,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        FileType fileType = FileType.of(type);
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean checkRes = checkParamsAndCode(response, key, ILLEGAL_KEY) && checkParamsAndCode(response, fileType, ILLEGAL_TYPE);
        if (!checkRes) {
            LOGGER.error("文件上传失败，key:{},name:{},type:{},原因:参数不正确", key, name, type);
            return JsonResult.newFailResult("参数不正确");
        }
        if (StringUtils.isEmpty(name)) {
            name = fileName;
        }
        String keyWithType = null;
        try {
            InputStreamWrapper fileIS = InputStreamWrapper.createBufferWrapper(file.getInputStream());
            //获取到图片的真实类型
            String imgRealType = DownloadUtils.handleImgRealType(fileType, contentType, fileIS);
            keyWithType = DownloadUtils.handleKeyForImg(key, imgRealType);
            //如果是图片，那么name换成key.realType
            name = DownloadUtils.handleImgName(name, imgRealType, key);
            String newFileName = StorageConfig.SWIFT_FOLDER + keyWithType;
            File saveFile = new File(newFileName);
            FileUtils.copyInputStreamToFile(fileIS, saveFile);
            String downUri = DownloadUtils.getDownloadUri("v2", keyWithType, name);
            return JsonResult.newSuccJsonResult(downUri);
        } catch (Exception e) {
            catchExceptionAndSet(request, response, e);
            LOGGER.error("v2版本上传失败,key[{}],type[{}]", key, type, e);
            return JsonResult.newFailResult("上传失败");
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
