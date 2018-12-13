package com.qunar.qfproxy.model;

import org.apache.commons.lang3.StringUtils;


public enum Platform {
    MAC("mac"), WINDOWS("qim_windows"), LINUX("qim_linux"), IOS("qim_ios"), ANDROID("qim_android"), UNKNOWN("");

    private String desc;

    Platform(String desc) {
        this.desc = desc;
    }

    public static Platform getPlatform(String platformDesc) {
        if (StringUtils.isEmpty(platformDesc)) {
            return UNKNOWN;
        }
        for (Platform platform : Platform.values()) {
            if (platform.desc.equals(platformDesc)) {
                return platform;
            }
        }
        return UNKNOWN;
    }
}
