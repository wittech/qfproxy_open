package com.qunar.qfproxy.model;


public enum FileType {
    AVATAR("avatar"), IMG("img"), FILE("file");

    private String type;

    FileType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static FileType of(String type) {
        for (FileType fileType : FileType.values()) {
            if (fileType.type.equals(type)) {
                return fileType;
            }
        }
        return null;
    }
}
