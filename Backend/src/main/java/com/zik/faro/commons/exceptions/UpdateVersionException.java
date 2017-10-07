package com.zik.faro.commons.exceptions;

public class UpdateVersionException extends Exception{
	static final long serialVersionUID = -1000000000010000001L;

    public UpdateVersionException(final String message){
    	super(message);
    }

    public UpdateVersionException(final Throwable throwable){
        super(throwable);
    }
}
