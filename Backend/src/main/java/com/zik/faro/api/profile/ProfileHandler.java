package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.*;

import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.FaroWebAppException;
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
    public FaroUser getProfile(){
        String userId = securityContext.getUserPrincipal().getName();
        FaroUser user = UserManagement.loadFaroUser(userId);
        if(user == null){
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "User not found " + userId);
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
