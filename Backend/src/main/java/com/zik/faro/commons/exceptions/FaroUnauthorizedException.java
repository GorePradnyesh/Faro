package com.zik.faro.commons.exceptions;

public class FaroUnauthorizedException extends Exception{
	static final long serialVersionUID = -1000000012000000020L;

    public FaroUnauthorizedException(final String message){
        super(message);
    }

    FaroUnauthorizedException(final Throwable throwable){
        super(throwable);
    }
}
