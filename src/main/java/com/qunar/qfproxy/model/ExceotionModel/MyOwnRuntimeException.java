package com.qunar.qfproxy.model.ExceotionModel;

/**
 * MyOwnRuntimeException
 *
 * @author binz.zhang
 * @date 2018/12/10
 */
public class MyOwnRuntimeException extends RuntimeException{
    public MyOwnRuntimeException() {
    }

    public MyOwnRuntimeException(String message) {
        super(message);
    }

    public MyOwnRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyOwnRuntimeException(Throwable cause) {
        super(cause);
    }

    public MyOwnRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
