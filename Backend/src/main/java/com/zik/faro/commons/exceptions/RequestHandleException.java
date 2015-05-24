package com.zik.faro.commons.exceptions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RequestHandleException implements ExceptionMapper<Throwable>
{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandleException.class);
    //TODO: Improve the exception handling cases
    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            logger.error("****** WEB APP EXCEPTION :" + t.getMessage(), t);

            return ((WebApplicationException)t).getResponse();
        } else {
            logger.error("****** UNCAUGHT EXCEPTION :" + t.getMessage(), t);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    // Add an entity, etc.
                    .build();
        }
    }
}