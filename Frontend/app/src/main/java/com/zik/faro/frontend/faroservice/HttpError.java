package com.zik.faro.frontend.faroservice;

public class HttpError {
    public int code;
    public String message;

    public HttpError(int code, final String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
