package com.zik.faro.api.friends;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.applogic.FriendManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FriendRelation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import java.util.ArrayList;
import java.util.List;

@Path(Constants.FRIENDS_PATH_CONST)
public class FriendsHandler {

    @Path(Constants.INVITE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> inviteFriend(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                                          @QueryParam(Constants.FARO_USER_ID_PARAM) final String userId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");

        //TODO: extract the actual user id from the signature. A valid signature means a valid user
        String requestingUserId = signature;
        String inviteeUsersId = userId;

        FriendManagement.inviteFriend(requestingUserId, inviteeUsersId);
        
        return JResponse.ok(Constants.HTTP_OK).build();
    }


    //TODO: response return List<MinUser> instead of List<FriendRelation>
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getFriends(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);
        //TODO: extract the actual user id from the signature. A valid signature means a valid user
        String userId = signature;
        List<MinUser> friends = FriendManagement.getFriendList(userId);

        return JResponse.ok(friends).build();
    }

    @Path(Constants.REMOVE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> unFriend(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                                      @QueryParam(Constants.FARO_USER_ID_PARAM) final String toBeRemovedUserId
                                      ){
        ParamValidation.validateSignature(signature);
        String requestingUserId = "userIdFromSignature";
        FriendManagement.removeFriend(requestingUserId, toBeRemovedUserId);
        return JResponse.ok(Constants.HTTP_OK).build();
    }

}
