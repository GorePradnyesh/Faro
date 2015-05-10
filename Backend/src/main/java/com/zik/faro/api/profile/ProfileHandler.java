package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.HTTP_OK;
import static com.zik.faro.commons.Constants.PROFILE_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.PROFILE_PATH_CONST;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.FaroUser;

@Path(PROFILE_PATH_CONST)
public class ProfileHandler {
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfile(){
        String userId = securityContext.getUserPrincipal().getName();
        FaroUser user;
		try {
			user = UserManagement.loadFaroUser(userId);
		} catch (DataNotFoundException e) {
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
