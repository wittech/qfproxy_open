package com.qunar.qfproxy.model;


public class JsonResult<T> {
    private boolean ret;
    private String msg;
    private T data;

    public JsonResult(boolean ret, String msg, T data) {
        this.ret = ret;
        this.msg = msg;
        this.data = data;
    }

    public JsonResult(boolean ret, String msg) {
        this.ret = ret;
        this.msg = msg;
    }

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static JsonResult newSuccResult() {
        return new JsonResult(true, null);
    }

    public static <T> JsonResult<T> newSuccJsonResult(T data) {
        return new JsonResult<>(true, null, data);
    }

    public static <T> JsonResult<T> newSuccJsonResult(String msg, T data) {
        return new JsonResult<>(true, msg, data);
    }

    public static JsonResult<?> newFailResult(String errorMsg) {
        return new JsonResult(false, errorMsg);
    }

    public static <T> JsonResult<T> newFailResult(String msg, T data) {
        return new JsonResult<>(false, msg, data);
    }
}
