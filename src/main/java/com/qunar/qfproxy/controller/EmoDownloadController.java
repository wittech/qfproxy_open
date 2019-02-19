package com.qunar.qfproxy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.qunar.qfproxy.constants.StorageConfig;
import com.qunar.qfproxy.model.EmoPackConf;
import com.qunar.qfproxy.service.DownloadService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * EmoDownloadController
 *
 * @author binz.zhang
 * @date 2019/1/23
 */
@Controller
@RequestMapping(value = {"/v1/emo/", "/v2/emo"})
public class EmoDownloadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmoDownloadController.class);


    @Autowired
    private DownloadService downloadService;

    @RequestMapping("/d/z/{packageName:.*}")
    @ResponseBody
    public void downloadEmotions(@PathVariable(value = "packageName") String packageName,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        if (!StringUtils.endsWithIgnoreCase(packageName, ".zip")) {
            packageName = StringUtils.join(packageName, ".zip");
        }
        LOGGER.info("get the emo package:{}", packageName);
        try {
            downloadService.downloadService(StorageConfig.SWIFT_FOLDER_EMO_PACKAGE + packageName, request, response);
        } catch (IOException e) {
            LOGGER.error("emo download fail", e);
        }
    }

    @RequestMapping("/d/e/{key:.*}")
    @ResponseBody
    public void downloadEmotion(
            @PathVariable(value = "key") String typeName,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String fileName = StorageConfig.SWIFT_FOLDER_EMO_PACKAGE+typeName;
        downloadService.downloadService(fileName, request, response);
    }

    @RequestMapping("/d/getEmoConfig")
    @ResponseBody
    public Object getPackageName() {
        LOGGER.info("download the emoConfig");
        EmoPackConf emoPackConf;
        {
            try {
                emoPackConf = genEmoConfig();
            } catch (IOException e) {
                LOGGER.error("get emo config error", e);
                return "";
            }
        }
        return emoPackConf.getData();
    }

    private EmoPackConf genEmoConfig() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("emoConfig.json");
        InputStream read = classPathResource.getInputStream();
        String config = new String(ByteStreams.toByteArray(read));
        ObjectMapper mapper = new ObjectMapper();
        EmoPackConf params = mapper.readValue(config, EmoPackConf.class);
        return params;
    }

}