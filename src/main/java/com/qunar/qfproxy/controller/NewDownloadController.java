package com.qunar.qfproxy.controller;

import com.qunar.qfproxy.constants.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


@Controller
@RequestMapping(value = {"/v1","v2"})
public class NewDownloadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewDownloadController.class);

    @RequestMapping(value = "/download/{key:.*}", method = RequestMethod.GET)
    public void download(
                @PathVariable(value = "key") String key,
                @RequestParam(value = "name", required = false) String name,
                HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, IOException {

        String fileName = key;
        if (fileName == null || fileName.trim().length() == 0) {
            resp.reset();
            resp.setContentType("text/plain;charset=utf-8");
            PrintWriter writer = resp.getWriter();
            writer.write("error:can't get the file name! 不能获取文件名");
            writer.flush();
            return;
        }
        fileName = StorageConfig.SWIFT_FOLDER + fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            resp.reset();
            resp.setContentType("text/plain;charset=utf-8");
            PrintWriter writer = resp.getWriter();
            writer.write("error: file not exist! 文件不存在");
            writer.flush();
        }
        long length = file.length();
        long start = 0;
        resp.reset();
        resp.setHeader("Accept-Ranges", "byte");
        String range = req.getHeader("Range");
        String mtype =  new MimetypesFileTypeMap().getContentType(fileName);
        resp.setHeader("Content-Type", mtype);
        resp.setHeader("Content-Length", new Long(length).toString());
        if (range != null) {
            resp.setHeader("Content-Range", "bytes " + new Long(start).toString() + "-" + new Long(start + length - 1).toString() + "/" + new Long(file.length()).toString());
        }
        resp.setContentType(mtype);
        resp.setHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes(), "utf-8"));
        long k = 0;
        int ibuffer = 65536;
        byte[] bytes = new byte[ibuffer];
        FileInputStream fileinputstream = new FileInputStream(file);
        try {

            OutputStream os = resp.getOutputStream();
            while (k < length) {
                int j = fileinputstream.read(bytes, 0, (int) (length - k < ibuffer ? length - k : ibuffer));
                if (j < 1) {
                    break;
                }
                os.write(bytes, 0, j);
                k += j;
            }
            os.flush();
            LOGGER.info("文件{},下载成功",fileName);
        } catch (Exception e) {
            LOGGER.error("文件下载异常,{}",e);
        } finally {
            fileinputstream.close();
        }
    }
}
