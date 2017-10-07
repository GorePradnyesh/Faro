package com.zik.faro.commons.exceptions;

import com.google.gson.Gson;
import com.zik.faro.commons.FaroErrorResponse;
import com.zik.faro.commons.FaroResponseStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by granganathan on 4/8/15.
 */
public class FaroWebAppException extends WebApplicationException {

    public FaroWebAppException(FaroResponseStatus faroResponseStatus, String message) {
    	super(Response.status(faroResponseStatus.getRestResponseStatus())
                .entity(new FaroErrorResponse(faroResponseStatus.getFaroStatusCode(),message)).
                	type(MediaType.APPLICATION_JSON_TYPE).build());
    }
    
    public FaroWebAppException(FaroResponseStatus faroResponseStatus) {
    	super(Response.status(faroResponseStatus.getRestResponseStatus())
                .entity(new FaroErrorResponse(faroResponseStatus.getFaroStatusCode(),
                		faroResponseStatus.getFaroStatusMessage())).type(MediaType.APPLICATION_JSON_TYPE).build());
    }

}
