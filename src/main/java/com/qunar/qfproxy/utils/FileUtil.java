package com.qunar.qfproxy.utils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileUtil
 *
 * @author binz.zhang
 * @date 2018/12/7
 */
public class FileUtil {

    /**
     * 获得文件扩展名（不带句点的）
     *  
     *
     * @param file 文件
     * @return
     */
    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    /**
     * 获得文件扩展名（不带句点的）
     *  
     *
     * @param fileName 文件名，可以含路径
     * @return
     */
    public static String getExtension(String fileName) {
        if ((fileName != null) && (fileName.length() > 0)) {
            int i = fileName.lastIndexOf('.');


            if ((i > -1) && (i < (fileName.length() - 1))) {
                return fileName.substring(i + 1);
            }
        }
        return null;
    }

    /**
     * 获得文件名（不带路径的）
     *  
     *
     * @param fileName 文件名，可以含路径
     * @return
     */
    public static String getFilename(String filePath) {
        String regExp = ".+\\\\(.+)$";
        ;
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(filePath);
        boolean result = m.find();
        if (result) {
            return m.group(1);
        }
        return filePath;
    }

    /**
     * 获得文件内容
     *  
     *
     * @param fileName 文件绝对路径
     * @return
     * @throws IOException 
     */
    public static String getContent(String fileName) throws IOException {

        StringBuffer strBuf = new StringBuffer();
        FileReader fr = null;
        BufferedReader br = null;
        File file = new File(fileName);
        if (file.exists()) {
            int readNum = 0;
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            char[] charBuffer = new char[1024];
            while ((readNum = br.read(charBuffer)) != -1) {
                strBuf.append(charBuffer, 0, readNum);
            }
            br.close();
            fr.close();
            return strBuf.toString();
        } else {
            return null;
        }
    }
}




