package com.zik.faro.commons.exceptions;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RequestHandleException implements ExceptionMapper<Throwable>
{
    //TODO: Change the system print to log statements once logging has been set up.
    //TODO: Improve the exception handling cases
    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            System.out.println("****** WEB APP EXCEPTION :" + t.getMessage());
            return ((WebApplicationException)t).getResponse();
        } else {
            System.out.println("****** UNCAUGHT EXCEPTION :" + t.getMessage());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    // Add an entity, etc.
                    .build();
        }
    }
}