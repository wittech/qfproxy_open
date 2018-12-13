package com.qunar.qfproxy.constants;


public class CodeConstants {
    public static final String X_QFPROXY_CODE = "X-QFProxy-Code";

    public static final int ILLEGAL_TYPE = 101;//非法的文件类型

    public static final int ILLEGAL_SIZE = 102;//非法的文件大小

    public static final int ILLEGAL_KEY = 103;//非法的文件key

    public static final int ILLEGAL_NAME = 104;//非法的文件名

    public static final int ILLEGAL_LOC = 105;//非法的文件存储位置

    public static final int ILLEGAL_RESIZE = 106;//非法的图片缩放参数

    public static final int SUCCESS = 200;//成功

    public static final int FILE_EXIST = 202;//文件检查时，文件已存在

    public static final int UPLOAD_FAIL = 304;//上传失败

    public static final int NO_AUTH = 500;//身份验证错误

    public static final int CAN_NOT_AUTH = 501;//无法进行身份验证

    public static final int IO_ERROR = 400;//IO错误

    public static final int TIMEOUT_ERROR = 401;//超时错误

    public static final int HOST_CONN_ERROR = 402;//http方式连接存储服务异常

    public static final int UNKNOWN_ERROR = 409;//未知错误
}
