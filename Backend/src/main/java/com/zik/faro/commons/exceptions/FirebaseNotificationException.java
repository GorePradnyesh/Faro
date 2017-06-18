package com.zik.faro.commons.exceptions;

public class FirebaseNotificationException extends Exception{
	static final long serialVersionUID = -1000000000100000020L;

    public FirebaseNotificationException(final String message){
        super(message);
    }

    FirebaseNotificationException(final Throwable throwable){
        super(throwable);
    }
}
