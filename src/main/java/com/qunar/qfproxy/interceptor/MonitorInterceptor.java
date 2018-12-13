package com.qunar.qfproxy.interceptor;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.qunar.qfproxy.constants.QMonitorConstants;
import com.qunar.qfproxy.model.Platform;
import com.qunar.qfproxy.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MonitorInterceptor extends HandlerInterceptorAdapter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorInterceptor.class);
    private static final Map<Platform, String> V2_PLATFORM = new HashMap<>(4);
    @Resource
    private PlatformHandler platformHandler;

    static {
        V2_PLATFORM.put(Platform.MAC, QMonitorConstants.V2_MAC);
        V2_PLATFORM.put(Platform.WINDOWS, QMonitorConstants.V2_WINDOWS);
        V2_PLATFORM.put(Platform.LINUX, QMonitorConstants.V2_LINUX);
        V2_PLATFORM.put(Platform.IOS, QMonitorConstants.V2_IOS);
        V2_PLATFORM.put(Platform.ANDROID, QMonitorConstants.V2_ANDROID);
        V2_PLATFORM.put(Platform.UNKNOWN, QMonitorConstants.V2_UNKNOWN);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        monitorV2Platform(request);
        return true;
    }

    private void monitorV2Platform(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> paths = Splitter.on('/').omitEmptyStrings().splitToList(path);
        if (paths.contains("v2")) {
            platformHandler.monitorPlatform(request, V2_PLATFORM, new Function<HttpServletRequest, Platform>() {
                @Nullable
                @Override
                public Platform apply(@Nullable HttpServletRequest input) {
                    if (input == null) {
                        return Platform.UNKNOWN;
                    }
                    String queryStr = input.getQueryString();
                    if (StringUtils.isEmpty(queryStr)) {
                        return Platform.UNKNOWN;
                    }

                    Map<String, List<String>> queryParams = HttpUtils.getHttpParams(queryStr);
                    String p = HttpUtils.getFirstParam(queryParams, "p");
                    return Platform.getPlatform(p);
                }
            });
        }
    }
}
