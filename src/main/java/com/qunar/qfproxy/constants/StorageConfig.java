package com.qunar.qfproxy.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


@Component
public class StorageConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageConfig.class);
    private static Properties props;
    public static final String SWIFT_FOLDER = getProperty("storage_folder");

    @PostConstruct
    private void initStoreFolder(){
        try {
            File f = new File(SWIFT_FOLDER);
            f.setWritable(true, false);
            f.mkdirs();
            LOGGER.info("upload文件件初始化创建成功");
        }catch (Exception e){
            LOGGER.error("upload文件件初始化创建失败！异常原因:{}",e);
        }
    }

    private synchronized static void init() {
        if (props != null) {
            return;
        }
        InputStreamReader isr = null;
        try {
            String filename = "storage.properties";
            isr = new InputStreamReader(StorageConfig.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
            props = new Properties();

            props.load(isr);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        } finally {
            closeStream(isr);
        }
    }

    public static String getProperty(String name) {
        if (props == null) {
            init();
        }
        String val = props.getProperty(name.trim());
        if (val == null) {
            return null;
        } else {
            //去除前后端空格
            return val.trim();
        }
    }

    private static void closeStream(InputStreamReader is) {
        if (is == null) {
            return;
        }

        try {
            is.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        }
    }
}
