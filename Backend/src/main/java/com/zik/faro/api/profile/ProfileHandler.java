package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.*;

import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path(PROFILE_PATH_CONST)
public class ProfileHandler {
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfile(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);

        String userId = signature;      //TODO: Extract userId from signature !!
        FaroUser user;
		try {
			user = UserManagement.loadFaroUser(userId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(404).entity("User not found " + userId).build();
            throw new WebApplicationException(response);
		}
       
        return user;
    }

    @Path(PROFILE_CREATE_PATH_CONST)
    @PUT
    public String createProfile(final FaroUser faroUser){
        //TODO: Verify that username in Signature and the faroUser.id is the same
        UserManagement.storeFaroUser(faroUser.getId(), faroUser);
        return HTTP_OK;
    }





}
