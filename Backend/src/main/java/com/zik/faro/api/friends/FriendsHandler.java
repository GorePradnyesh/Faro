package com.zik.faro.api.friends;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.applogic.FriendManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FriendRelation;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

@Path(Constants.FRIENDS_PATH_CONST)
public class FriendsHandler {
    @Context
    SecurityContext securityContext;

    @Path(Constants.INVITE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> inviteFriend(final String friendId){
        //ParamValidation.genericParamValidations(friendId, "userId");

        String requestingUserId = securityContext.getUserPrincipal().getName();
       
        try {
			FriendManagement.inviteFriend(requestingUserId, friendId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		} catch (IllegalDataOperation e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
        
        return JResponse.ok(Constants.HTTP_OK).build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getFriends(){
        String userId = securityContext.getUserPrincipal().getName();
        List<MinUser> friends = FriendManagement.getFriendList(userId);

        return JResponse.ok(friends).build();
    }

    @Path(Constants.REMOVE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> unFriend(@QueryParam(Constants.FARO_USER_ID_PARAM) final String toBeRemovedUserId
                                      ){
    	ParamValidation.genericParamValidations(toBeRemovedUserId, Constants.FARO_USER_ID_PARAM);
    	String requestingUserId = securityContext.getUserPrincipal().getName();
    	try {
			FriendManagement.removeFriend(requestingUserId, toBeRemovedUserId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		} catch (IllegalDataOperation e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
    	return JResponse.ok(Constants.HTTP_OK).build();
    }

}
