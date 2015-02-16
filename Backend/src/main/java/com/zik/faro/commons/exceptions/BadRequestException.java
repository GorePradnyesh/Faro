package com.zik.faro.commons.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class BadRequestException extends WebApplicationException {
    public BadRequestException(final String message){
        //TODO: Construct the response in the format, if any, requested by the "Accept" header
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
