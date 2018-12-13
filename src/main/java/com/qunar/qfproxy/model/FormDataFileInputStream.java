package com.qunar.qfproxy.model;

import com.google.common.base.Splitter;
import com.qunar.qfproxy.model.ExceotionModel.MyOwnRuntimeException;
import com.qunar.qfproxy.utils.HttpUtils;
import com.qunar.qfproxy.utils.StreamUtils;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;



/**
 * v2版本的主类，用于支持边从客户端获取数据，边上传至存储服务，该类涉及的http post form-data的rfc协议规定的格式的解析
 */
public class FormDataFileInputStream extends ServletInputStream {
    public static final byte CR = 0x0D;
    public static final byte LF = 0x0A;
    public static final byte DASH = 0x2D;
    protected static final byte[] STREAM_TERMINATOR = {DASH, DASH};
    protected static final byte[] HEADER_SEPARATOR = {CR, LF, CR, LF};
    public static final int HEADER_PART_SIZE_MAX = 10240;

    private InputStream internSiS;
    private byte[] cache;
    private int regionLen = -1;
    private int pos = -1;
    private int tail = -1;
    private byte[] boundary;
    private int bLoc = -1;
    private String fileName;
    private String fieldName;
    private String contentType;
    private String formDataHeaders;
    private boolean finished = false;
    public String httpContentType;
    private List<Header> filedHeaders;


    public FormDataFileInputStream(String httpContentType, ServletInputStream internSiS) {
        this.httpContentType = httpContentType;
        this.internSiS = internSiS;
        setBoundaryByte(httpContentType);
        cache = new byte[1048576];
    }

    public FormDataFileInputStream(String httpContentType, ServletInputStream internSiS, boolean readHeadersInPostBody) throws IOException {
        this(httpContentType, internSiS);
        if (readHeadersInPostBody) {
            if (StringUtils.isEmpty(formDataHeaders)) {
                readFrontBoundary();
                formDataHeaders = readHeaders();
                setFieldHeaders(formDataHeaders);
            }
        }
    }

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException();
//        return internSiS.isFinished();
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public int read() throws IOException {
        if (StringUtils.isEmpty(formDataHeaders)) {
            readFrontBoundary();
            formDataHeaders = readHeaders();
            setFieldHeaders(formDataHeaders);
        }
        if (regionLen < 0) {
            doCache();
        }
        if (regionLen < 0) {
            return -1;
        }
        int b = readCacheByte();
        if (b < 0) {
            doCache();
            b = finished ? -1 : readCacheByte();
        }
        return b < 0 ? -1 : b;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getContentType() {
        return contentType;
    }


    private void findBoundary() {
        bLoc = sundayMatch(cache, 0, regionLen, boundary);
    }

    private void doCache() throws IOException {
        if (finished) {
            StreamUtils.consumeRest(internSiS);
        }
        if (regionLen < 0) {
            regionLen = internSiS.read(cache);
        } else {
            System.arraycopy(cache, regionLen - boundary.length + 1, cache, 0, boundary.length - 1);
            int temp = internSiS.read(cache, boundary.length - 1, cache.length - boundary.length + 1);
            if (temp > 0) {
                regionLen = temp + boundary.length - 1;
            } else {
                finished = true;
            }

        }
        if (!finished) {
            pos = 0;
            tail = regionLen - boundary.length;
            findBoundary();

            //只读第一个参数,其他的舍弃
            if (bLoc >= 0) {
                finished = true;
            }
        }

    }


    private int readCacheByte() throws IOException {
        if (pos < 0 || pos > tail || (bLoc >= 0 && pos >= bLoc)) {
            return -1;
        }
        return cache[pos++] & 0xff;
    }

    public void setHttpContentType(String httpContentType) {
        this.httpContentType = httpContentType;
    }

    private void setBoundaryByte(String httpContentType) {
        if (StringUtils.isEmpty(httpContentType)) {
            throw new MyOwnRuntimeException("请求类型不是multipart类型");
        }

        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(httpContentType, new char[]{';', ','});
        String boundaryStr = params.get("boundary");

        if (boundaryStr == null) {
            throw new MyOwnRuntimeException("http header[content-type]没有boundary");
        }

        byte[] tempBoundary;
        try {
            tempBoundary = boundaryStr.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            tempBoundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
        }

        boundary = new byte[tempBoundary.length + 4];
        boundary[0] = CR;
        boundary[1] = LF;
        boundary[2] = DASH;
        boundary[3] = DASH;
        System.arraycopy(tempBoundary, 0, boundary, 4, tempBoundary.length);

    }


    private void setFieldHeaders(String headerPart) {
        final int len = headerPart.length();
        filedHeaders = new ArrayList<>();
        int start = 0;
        for (; ; ) {
            int end = parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            StringBuilder header = new StringBuilder(headerPart.substring(start, end));
            start = end + 2;
            while (start < len) {
                int nonWs = start;
                while (nonWs < len) {
                    char c = headerPart.charAt(nonWs);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                    ++nonWs;
                }
                if (nonWs == start) {
                    break;
                }
                // Continuation line found
                end = parseEndOfLine(headerPart, nonWs);
                header.append(" ").append(headerPart.substring(nonWs, end));
                start = end + 2;
            }
            parseHeaderLine(header.toString());
        }
        setFields();
    }

    private void setFields() {
        for (Header header : filedHeaders) {
            if ("content-disposition".equals(header.getName().trim().toLowerCase())) {
                String value = header.getValue();
                value = StringUtils.replace(value, "form-data;", "", 1).trim();
                Map<String, String> kv = Splitter.on(';').omitEmptyStrings().trimResults().withKeyValueSeparator('=').split(value);
                fieldName = HttpUtils.trim(kv.get("name"), '\"');
                fileName = HttpUtils.trim(kv.get("filename"), '\"');
            } else if ("content-type".equals(header.getName().trim().toLowerCase())) {
                contentType = header.getValue().trim();
            }
        }
    }

    private int parseEndOfLine(String headerPart, int end) {
        int index = end;
        for (; ; ) {
            int offset = headerPart.indexOf('\r', index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException(
                        "Expected headers to be terminated by an empty line.");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }

    private void parseHeaderLine(String header) {
        final int colonOffset = header.indexOf(':');
        if (colonOffset == -1) {
            // This header line is malformed, skip it.
            return;
        }
        String headerName = header.substring(0, colonOffset).trim();
        String headerValue =
                header.substring(header.indexOf(':') + 1).trim();
        filedHeaders.add(new BasicHeader(headerName, headerValue));
    }

    @SuppressWarnings("all")
    private boolean readFrontBoundary() throws IOException {
        for (int i = 0; i < boundary.length - 2; i++) {
            int t = internSiS.read();
            if (t < 0) {
                throw new RuntimeException("");
            }
            if (t != boundary[i + 2]) {
                throw new RuntimeException("boundary不相等");
            }
        }

        byte[] next = new byte[2];
        int read = internSiS.read(next);
        if (read < 2) {
            throw new RuntimeException("格式错误");
        }
        return Arrays.equals(next, STREAM_TERMINATOR);
    }

    private String readHeaders() throws IOException {
        int i = 0;
        byte b;
        // to support multi-byte characters
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        int size = 0;
        while (i < HEADER_SEPARATOR.length) {
            b = (byte) internSiS.read();

            if (++size > HEADER_PART_SIZE_MAX) {
                throw new MyOwnRuntimeException(
                        format("Header section has more than %s bytes (maybe it is not properly terminated)",
                                HEADER_PART_SIZE_MAX));
            }
            if (b == HEADER_SEPARATOR[i]) {
                i++;
            } else {
                i = 0;
            }
            baOs.write(b);
        }

        String headers;
        try {
            headers = baOs.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            // Fall back to platform default if specified encoding is not
            // supported.
            headers = baOs.toString();
        }

        return headers;
    }

    //利用模式匹配的Sunday算法提高匹配效率
    private int sundayMatch(byte[] all, int allStart, int allEnd, byte[] pattern) {
        int i = 0;
        int allLen = allEnd - allStart + 1;
        while (i < allLen) {
            int j = 0;
            while (j < pattern.length && i + j < allLen && all[i + j] == pattern[j]) { //match
                j++;
            }
            if (j == pattern.length) {
                return i;
            } else {
                if (i + pattern.length < allLen) {
                    for (j = pattern.length - 1; j >= 0; j--) {
                        if (pattern[j] == all[i + pattern.length]) {
                            break;
                        }
                    }
                }
                i += pattern.length - j;
            }
        }

        return -1;
    }


}
