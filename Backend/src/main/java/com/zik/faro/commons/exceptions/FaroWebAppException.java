package com.zik.faro.commons.exceptions;

import com.zik.faro.commons.FaroResponseStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by granganathan on 4/8/15.
 */
public class FaroWebAppException extends WebApplicationException {
    private FaroResponseStatus faroResponseStatus;

    public FaroWebAppException(FaroResponseStatus faroResponseStatus) {
        this(faroResponseStatus, "");
    }

    public FaroWebAppException(FaroResponseStatus faroResponseStatus, String message) {
        super(Response.status(faroResponseStatus.getRestResponseStatus())
                .entity(faroResponseStatus.getFaroResponseEntity()).type(MediaType.APPLICATION_JSON_TYPE).build());
        this.faroResponseStatus = faroResponseStatus;
    }

    public FaroResponseStatus getFaroResponseStatus() {
        return faroResponseStatus;
    }
}
