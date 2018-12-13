package com.qunar.qfproxy.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;


public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static void addHeaders(final HttpServletResponse response, List<Header> headers) {
        if (headers == null || headers.size() == 0) {
            return;
        }
        for (Header header : headers) {
            response.addHeader(header.getName(), header.getValue());
        }
    }

//    public static void addHeaders(final HttpServletResponse response, List<Header> headers, List<String>)

    public static List<Header> getHeadersWithoutHost(HttpServletRequest request) {
        Enumeration<String> headerNameEnumeration = request.getHeaderNames();
        List<Header> headers = new ArrayList<>();
        while (headerNameEnumeration.hasMoreElements()) {
            String headerName = headerNameEnumeration.nextElement();
            if (!HttpHeaders.HOST.equals(headerName)) {
                headers.add(new BasicHeader(headerName, request.getHeader(headerName)));
            }
        }
        return headers;
    }

    public static void addHeaders(final HttpServletResponse response, List<Header> headers, boolean retainDownload) {
        if (retainDownload) {
            addHeaders(response, headers);
        } else {
            if (headers == null || headers.size() == 0) {
                return;
            }

            for (Header header : headers) {
                if (!StringUtils.equalsIgnoreCase(header.getName(), HttpHeaders.CONTENT_DISPOSITION)) {
                    response.addHeader(header.getName(), header.getValue());
                }
            }
        }
    }

    public static String urlEncode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String urlDecode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static Map<String, List<String>> getHttpParams(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return null;
        }

        Map<String, List<String>> result = new HashMap<>();
        List<String> split = Splitter.on("&").omitEmptyStrings().trimResults().splitToList(queryString);
        for (String kv : split) {
            int index = kv.indexOf("=");
            if (index > 0 & index < kv.length() - 1) {
                String key = kv.substring(0, index);
                String value = kv.substring(index + 1, kv.length());
                putHttpParam(result, key, value);
            }
        }
        return result;
    }

    public static String getFirstParam(Map<String, List<String>> params, String key) {
        if (MapUtils.isEmpty(params)) {
            return null;
        }
        List<String> valuesList = params.get(key);
        if (CollectionUtils.isEmpty(valuesList)) {
            return null;
        } else {
            return urlDecode(valuesList.get(0));
        }
    }

    public static String trim(String str, char c) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return str.substring(str.charAt(0) == c ? 1 : 0, str.charAt(str.length() - 1) == c ? str.length() - 1 : str.length());
    }

    private static void putHttpParam(final Map<String, List<String>> params, String key, String value) {
        if (params == null) {
            return;
        }
        List<String> valueList = params.get(key);
        if (valueList == null) {
            params.put(key, Lists.newArrayList(value));
        } else {
            valueList.add(value);
        }
    }

    public static String setToUrlStr(String urlStr, String defaultProtocolAndDomain) {
        try {
            new URL(urlStr);
            return urlStr;
        } catch (MalformedURLException e) {
            if (urlStr.startsWith("/")) {
                return StringUtils.join(defaultProtocolAndDomain, urlStr);
            } else {
                return null;
            }
        }
    }

    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    LOGGER.error("", e);
                }
                ip = inet != null ? inet.getHostAddress() : null;
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) { // "***.***.***.***".length() = 15
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }
}
