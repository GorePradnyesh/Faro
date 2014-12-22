package com.zik.faro.commons.exceptions;

public class DataNotFoundException extends Exception{
    static final long serialVersionUID = -1000000000000000001L;

    public DataNotFoundException(final String message){
        super(message);
    }

    DataNotFoundException(final Throwable throwable){
        super(throwable);
    }
}
