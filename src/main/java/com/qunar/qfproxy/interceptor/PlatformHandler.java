package com.qunar.qfproxy.interceptor;

import com.google.common.base.Function;
import com.qunar.qfproxy.model.Platform;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
public class PlatformHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformHandler.class);

    public void monitorPlatform(HttpServletRequest request, Map<Platform, String> indexMap, Function<HttpServletRequest, Platform> function) {
        if (MapUtils.isEmpty(indexMap)) {
            return;
        }

        try {
            Platform platform = function.apply(request);
            String index = indexMap.get(platform);
            count(index);
        } catch (Exception e) {
            LOGGER.error("记录平台信息时出错", e);
            count(indexMap.get(Platform.UNKNOWN));
        }

    }

    private void count(String index) {
        if (StringUtils.isNotEmpty(index)) {
         //   QMonitor.recordOne(index);
        }
    }
}
