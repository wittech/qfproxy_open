package com.qunar.qfproxy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


public class StreamUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamUtils.class);

    /**
     * 消费掉<code>InputStream</code>中剩余的资源
     * <note>
     * 并没有调用<code>is.close()</code>方法
     * </note>
     *
     * @param is 输入流
     * @see java.io.InputStream
     */
    public static void consumeRest(InputStream is) {
        try {
            int read = is.read();
            while (read >= 0) {
                read = is.read();
            }
        } catch (IOException e) {
            LOGGER.error("consumer rest inputStream error");
        }
    }
}
