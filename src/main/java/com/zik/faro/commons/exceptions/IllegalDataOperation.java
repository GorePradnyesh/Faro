package com.zik.faro.commons.exceptions;

public class IllegalDataOperation extends Exception {
    static final long serialVersionUID = -1000000000000000000L;

    public IllegalDataOperation(final String message){
        super(message);
    }

    IllegalDataOperation(final Throwable throwable){
        super(throwable);
    }
}
