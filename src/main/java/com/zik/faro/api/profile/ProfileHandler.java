package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.*;

import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(PROFILE_PATH_CONST)
public class ProfileHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfile(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);

        String userId = signature;      //TODO: Extract userId from signature !!
        FaroUser user = UserManagement.loadFaroUser(userId);
        if(user == null){
            Response response = Response.status(404).entity("User not found " + userId).build();
            throw new WebApplicationException(response);
        }
        return user;
    }

    @Path(PROFILE_CREATE_PATH_CONST)
    @PUT
    public String createProfile(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature, final FaroUser faroUser){
        ParamValidation.validateSignature(signature);
        //TODO: Verify that username in Signature and the faroUser.id is the same
        UserManagement.storeFaroUser(faroUser.getId(), faroUser);
        return HTTP_OK;
    }





}
