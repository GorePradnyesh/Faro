package com.zik.faro.commons.exceptions;

public class UpdateException extends Exception{
	static final long serialVersionUID = -1000000000110000001L;

    public UpdateException(final String message){
    	super(message);
    }

    public UpdateException(final Throwable throwable){
        super(throwable);
    }
}
