package com.zik.faro.commons.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by granganathan on 3/2/15.
 */
public class InvalidLoginException extends WebApplicationException {
    public InvalidLoginException (String message) {
        super(Response.status(Response.Status.UNAUTHORIZED)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
