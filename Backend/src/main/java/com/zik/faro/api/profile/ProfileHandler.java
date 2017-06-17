package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.*;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

@Path(PROFILE_PATH_CONST)
public class ProfileHandler {
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<FaroUser> getProfile(){
        String userId = securityContext.getUserPrincipal().getName();
        FaroUser user;
		try {
			user = UserManagement.loadFaroUser(userId);
		} catch (DataNotFoundException e) {
			throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "User not found " + userId);
		}
     
        return JResponse.ok(user).build();
    }

    @Path(PROFILE_CREATE_PATH_CONST)
    @PUT
    public JResponse<String> createProfile(final FaroUser faroUser){
        //TODO: Verify that username in Signature and the faroUser.id is the same
        UserManagement.storeFaroUser(faroUser.getId(), faroUser);
        return JResponse.ok(Constants.HTTP_OK).build();
    }
    
    @Path(PROFILE_UPSERT_PATH_CONST)
    @PUT
    public JResponse<FaroUser> upsertProfile(final FaroUser faroUser){
    	String userId = securityContext.getUserPrincipal().getName();
    	try{
    		return JResponse.ok(UserManagement.upsertFaroUser(faroUser, userId)).build();
    	}catch (DataNotFoundException e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        } catch (DatastoreException e) {
        	Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(e.getMessage())
                     .build();
            throw new WebApplicationException(response);
		} catch (UpdateVersionException e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
           throw new WebApplicationException(response);
		}
    	    
    }
    
    @Path(PROFILE_ADD_USER_REGISTRATION_TOKEN)
    @PUT
    public JResponse<String> addRegistrationToken(final String registrationToken){
    	String userId = securityContext.getUserPrincipal().getName();
    	try {
			UserManagement.addRegistrationToken(userId, registrationToken);
    	}catch (DataNotFoundException e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        } catch (DatastoreException e) {
        	Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(e.getMessage())
                     .build();
            throw new WebApplicationException(response);
		} catch (UpdateVersionException e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
           throw new WebApplicationException(response);
		}
    	return JResponse.ok(Constants.HTTP_OK).build();
    }
    
    
    @Path(PROFILE_REMOVE_USER_REGISTRATION_TOKEN)
    @PUT
    public JResponse<String> removeRegistrationToken(final String registrationToken){
    	String userId = securityContext.getUserPrincipal().getName();
    	try {
			UserManagement.removeRegistrationToken(userId, registrationToken);
    	}catch (DataNotFoundException e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        } catch (DatastoreException e) {
        	Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(e.getMessage())
                     .build();
            throw new WebApplicationException(response);
		} catch (UpdateVersionException e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
           throw new WebApplicationException(response);
		}
    	return JResponse.ok(Constants.HTTP_OK).build();
    }

}
