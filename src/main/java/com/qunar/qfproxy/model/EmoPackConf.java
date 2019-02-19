package com.qunar.qfproxy.model;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * EmoPackConf
 *
 * @author binz.zhang
 * @date 2019/1/30
 */
@Service
public class EmoPackConf {

    private List<configContent> data;

    public List<configContent> getData() {
        return data;
    }

    public void setData(List<configContent> data) {
        this.data = data;
    }

    public static class configContent {
        private String pkgid;
        private String name;
        private String file;
        private String desc;
        private String thumb;
        private Integer file_size;
        private String md5;

        public String getPkgid() {
            return pkgid;
        }

        public String getName() {
            return name;
        }

        public String getFile() {
            return file;
        }

        public String getDesc() {
            return desc;
        }

        public String getThumb() {
            return thumb;
        }

        public Integer getFile_size() {
            return file_size;
        }

        public String getMd5() {
            return md5;
        }

        public void setPkgid(String pkgid) {
            this.pkgid = pkgid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setThumb(String thump) {
            this.thumb = thump;
        }

        public void setFile_size(Integer file_size) {
            this.file_size = file_size;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }
    }


}
