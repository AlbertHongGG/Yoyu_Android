package com.jasonhong.yoyu.core.network;

public class ApiException extends Exception {
    private final String code;

    public ApiException(String message) {
        super(message);
        this.code = "";
    }

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
