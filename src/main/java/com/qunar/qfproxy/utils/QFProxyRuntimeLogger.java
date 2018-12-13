package com.qunar.qfproxy.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


public class QFProxyRuntimeLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(QFProxyRuntimeLogger.class);

    public static void log(Level logLevel, Exception e, String format, Object... objects) {
        if (Level.DEBUG.equals(logLevel)) {
            LOGGER.debug(format, objects, e);
        } else if (Level.INFO.equals(logLevel)) {
            LOGGER.info(format, objects, e);
        } else if (Level.WARN.equals(logLevel)) {
            LOGGER.warn(format, objects, e);
        } else if (Level.ERROR.equals(logLevel)) {
            LOGGER.error(format, objects, e);
        }
    }

    public static void log(String format, Object... objects) {
        log(Level.INFO, null, format, objects);
    }

    public static void log(HttpServletRequest request, Integer errorCode, Exception ex, Logger logger) {
        Logger locLogger = logger == null ? LOGGER : logger;
        if (request == null) {
            if (errorCode != null || ex != null) {
                locLogger.error("error code [{}]", errorCode, ex);
            }
        } else {

            if (errorCode != null || ex != null) {
                locLogger.error("error code [{}],request method [{}],request uri [{}],request query [{}] user_agent [{}],request host [{}],remote ip [{}],nginx ip [{}]",
                        errorCode, request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        request.getHeader("User-Agent"), request.getHeader("Host"), getIp(request), request.getRemoteAddr(), ex);
            } else {
                locLogger.info("request method [{}],request uri [{}],request query [{}] user_agent [{}],request host [{}],remote ip [{}],nginx ip [{}]",
                        request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        request.getHeader("User-Agent"), request.getHeader("Host"), getIp(request), request.getRemoteAddr());
            }

        }

    }

    public static String getIp(HttpServletRequest request) {
        return HttpUtils.getIp(request);
    }
}
