package com.qunar.qfproxy.servlet;

import com.qunar.qfproxy.constants.Config;
import com.qunar.qfproxy.constants.HttpConstants;
import com.qunar.qfproxy.constants.StorageConfig;
import com.qunar.qfproxy.model.FileType;
import com.qunar.qfproxy.model.FormDataFileInputStream;
import com.qunar.qfproxy.model.JsonResult;
import com.qunar.qfproxy.utils.DownloadUtils;
import com.qunar.qfproxy.utils.HttpUtils;
import com.qunar.qfproxy.utils.JacksonUtil;
import com.qunar.qfproxy.utils.QFProxyRuntimeLogger;
import com.qunar.qfproxy.utils.imgtype.InputStreamWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.qunar.qfproxy.constants.CodeConstants.ILLEGAL_KEY;
import static com.qunar.qfproxy.constants.CodeConstants.ILLEGAL_TYPE;
import static com.qunar.qfproxy.utils.ErrorCodeUtil.catchExceptionAndSet;
import static com.qunar.qfproxy.utils.ErrorCodeUtil.checkParamsAndCode;


public class UploadV2Servlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadV2Servlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req,resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String queryStr = req.getQueryString();
        String requestURI = req.getRequestURI();
        ServletInputStream si = req.getInputStream();
        FormDataFileInputStream fdFi = new FormDataFileInputStream(req.getContentType(), si, true);
        Map<String, List<String>> params = HttpUtils.getHttpParams(queryStr);
        String type = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.length());
        String key = getParam(params, "key");
        String name = getParam(params, "name");
        if (StringUtils.isEmpty(key)) {
            LOGGER.error("上传错误，没有key:[{}]参数", key);
            resp.setStatus(400);
            resp.getOutputStream().write("没有key或者size".getBytes(Charset.forName("utf-8")));
            return;
        }
        JsonResult<?> jr = doUploadV2(key, type, name, fdFi.getFileName(), fdFi.getContentType(), fdFi, req, resp);

        QFProxyRuntimeLogger.log(req, null, null, null);
        if (jr == null || !jr.isRet()) {
            LOGGER.error("上传过程中出错,key:[{}],name:[{}]", key, name);
            resp.getOutputStream().write(JacksonUtil.obj2String(new JsonResult<String>(false, "上传错误", null)).getBytes(Charset.forName("utf-8")));
            return;
        }
        LOGGER.info("文件{}上传成功", key);
        resp.getOutputStream().write(JacksonUtil.obj2String(jr).getBytes(Charset.forName("utf-8")));
    }

    private JsonResult<?> doUploadV2(String key, String type, String name, String fileName,
                                     String contentType, InputStream is, HttpServletRequest request, HttpServletResponse response) {
        FileType fileType = FileType.of(type);
        boolean checkRes = checkParamsAndCode(response, key, ILLEGAL_KEY) && checkParamsAndCode(response, fileType, ILLEGAL_TYPE);
        if (!checkRes) {
            LOGGER.error("文件上传失败，key:{},name:{},type:{},原因:参数不正确", key, name, type);
            return JsonResult.newFailResult("参数不正确");
        }
        if (StringUtils.isEmpty(name)) {
            name = fileName;
        }
        try {
            InputStreamWrapper fileIS = InputStreamWrapper.createBufferWrapper(is);
            //获取到图片的真实类型
            String imgRealType = DownloadUtils.handleImgRealType(fileType, contentType, fileIS);
            String keyWithType = DownloadUtils.handleKeyForImg(key, imgRealType);
            //如果是图片，那么name换成key.realType
            name = DownloadUtils.handleImgName(name, imgRealType, key);
            String newFileName = StorageConfig.SWIFT_FOLDER + keyWithType;
            File saveFile = new File(newFileName);
            FileUtils.copyInputStreamToFile(fileIS, saveFile);
            String downUri = DownloadUtils.getDownloadUri("v2", keyWithType, name);
            LOGGER.info("return the download url {}",downUri);
            return JsonResult.newSuccJsonResult(downUri);
        } catch (Exception e) {
            catchExceptionAndSet(request, response, e);
            LOGGER.error("v2版本上传失败,key[{}],type[{}]", key, type, e);
            return JsonResult.newFailResult("上传失败");
        }
    }

    private String getParam(Map<String, List<String>> params, String name) {
        if (params == null) {
            return null;
        }
        return HttpUtils.getFirstParam(params, name);
    }


}
