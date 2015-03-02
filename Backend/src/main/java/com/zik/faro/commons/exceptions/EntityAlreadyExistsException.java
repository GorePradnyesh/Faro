package com.zik.faro.commons.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by granganathan on 2/22/15.
 */
public class EntityAlreadyExistsException extends WebApplicationException {
    public EntityAlreadyExistsException(String message) {
        super(Response.status(Response.Status.CONFLICT)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
