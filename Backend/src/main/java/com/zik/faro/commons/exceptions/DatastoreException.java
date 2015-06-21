package com.zik.faro.commons.exceptions;

public class DatastoreException extends Exception{
	static final long serialVersionUID = -1000000000000000020L;

    public DatastoreException(final String message){
        super(message);
    }

    DatastoreException(final Throwable throwable){
        super(throwable);
    }
}
