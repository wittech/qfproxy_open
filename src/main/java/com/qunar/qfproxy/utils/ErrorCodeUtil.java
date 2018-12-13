package com.qunar.qfproxy.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qunar.qfproxy.constants.CodeConstants.*;


public class ErrorCodeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorCodeUtil.class);
    private static final Pattern PATTERN = Pattern.compile("^(/d+)x(/d+)$");


    public static class ErrorCodeHeader {
        private String value;

        public void setValue(String value) {
            if (StringUtils.isEmpty(value)) {
                this.value = value;
            } else {
                StringUtils.join(value, ",", value);
            }
        }

        public String getValue() {
            return value;
        }
    }

    public static boolean checkParamsAndCode(HttpServletResponse response, Object param, int errorCode) {
        switch (errorCode) {
            case ILLEGAL_KEY:
                if (StringUtils.isBlank((String) param)) {
                    setErrorCode(response, ILLEGAL_KEY);
                    return false;
                }
                return true;
            case ILLEGAL_SIZE:
                if ((long) param <= 0) {
                    setErrorCode(response, ILLEGAL_SIZE);
                    return false;
                }
                return true;
            case ILLEGAL_TYPE:
                if (param == null) {
                    setErrorCode(response, ILLEGAL_TYPE);
                    return false;
                }
                return true;
            case ILLEGAL_NAME:
                if (StringUtils.isBlank((String) param)) {
                    setErrorCode(response, ILLEGAL_NAME);
                    return false;
                }
                return true;
            case ILLEGAL_LOC:
                if (param == null) {
                    setErrorCode(response, ILLEGAL_LOC);
                    return false;
                }
                return true;
            case ILLEGAL_RESIZE:
                if (param != null) {
                    Matcher matcher = PATTERN.matcher((CharSequence) param);
                    if (!matcher.matches()) {
                        setErrorCode(response, ILLEGAL_RESIZE);
                        return false;
                    }
                }
                return true;
            default:
                return true;

        }
    }

    public static void catchExceptionAndSet(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof HttpHostConnectException) {
            setErrorCode(response, HOST_CONN_ERROR);
            QFProxyRuntimeLogger.log(request, HOST_CONN_ERROR, exception, LOGGER);
        } else if (exception instanceof IOException) {
            setErrorCode(response, IO_ERROR);
            QFProxyRuntimeLogger.log(request, IO_ERROR, exception, LOGGER);
        } else if (exception instanceof TimeoutException) {
            setErrorCode(response, TIMEOUT_ERROR);
            QFProxyRuntimeLogger.log(request, TIMEOUT_ERROR, exception, LOGGER);
        } else {
            setErrorCode(response, UNKNOWN_ERROR);
            QFProxyRuntimeLogger.log(request, UNKNOWN_ERROR, exception, LOGGER);
        }
//        LOGGER.error("", exception);
    }

    private static void setErrorCode(final HttpServletResponse response, int errorCode) {
//        String oriErrorCodes = response.getHeader(X_QFPROXY_CODE);
//        StringUtils.replace(oriErrorCodes,String.valueOf(CodeConstants.SUCCESS),"");
//        if (StringUtils.isBlank(oriErrorCodes)) {
//            response.setHeader(X_QFPROXY_CODE, String.valueOf(errorCode));
//        } else {
//            response.setHeader(X_QFPROXY_CODE, StringUtils.join(oriErrorCodes, ',', errorCode));
//        }
        if (!response.containsHeader(X_QFPROXY_CODE)) {
            response.setIntHeader(X_QFPROXY_CODE, errorCode);
        }
    }

}
