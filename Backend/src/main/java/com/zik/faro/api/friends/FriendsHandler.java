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
    public JResponse<String> inviteFriend(@QueryParam(Constants.FARO_USER_ID_PARAM) final String userId){
        ParamValidation.genericParamValidations(userId, "userId");

        String requestingUserId = securityContext.getUserPrincipal().getName();
        String inviteeUsersId = userId;

        try {
            FriendManagement.inviteFriend(requestingUserId, inviteeUsersId);
        } catch (IllegalDataOperation illegalDataOperation) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, illegalDataOperation.getMessage());
        } catch (DataNotFoundException e) {
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, e.getMessage());
        }
        return JResponse.ok(Constants.HTTP_OK).build();
    }


    //TODO: response return List<MinUser> instead of List<FriendRelation>
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
    public JResponse<String> unFriend(@QueryParam(Constants.FARO_USER_ID_PARAM) final String userId){
        ParamValidation.genericParamValidations(userId, "userId");

        String fromUser = securityContext.getUserPrincipal().getName();
        String toUser = userId;
        FriendManagement.deleteFriendRelationship(fromUser, toUser);

        //TODO: Replace below static response with actual code
        return JResponse.ok(Constants.HTTP_OK).build();
    }

}
