package com.zik.faro.commons;

import javax.ws.rs.core.Response;

/**
 * Created by granganathan on 4/8/15.
 */
public enum FaroResponseStatus {
    BAD_REQUEST(Response.Status.BAD_REQUEST, 1001, "Bad Request"),
    ENTITY_EXISTS(Response.Status.CONFLICT, 2001, "Entity already exists"),
    INVALID_LOGIN(Response.Status.UNAUTHORIZED, 3001, "Invalid login credentials"),
    UNAUTHORIZED(Response.Status.UNAUTHORIZED, 3002, "Not authorized to proceed with the operation"),
    NOT_FOUND(Response.Status.NOT_FOUND, 4001, "Not found element/data");

    private Response.Status restResponseStatus;
    private FaroResponseEntity faroResponseEntity;

    private FaroResponseStatus(Response.Status restResponseStatus, int faroStatusCode, String faroStatusMessage) {
        this.restResponseStatus = restResponseStatus;
        this.faroResponseEntity = new FaroResponseEntity(faroStatusCode, faroStatusMessage);
    }

    public Response.Status getRestResponseStatus() {
        return restResponseStatus;
    }

    public FaroResponseEntity getFaroResponseEntity() {
        return faroResponseEntity;
    }
}
