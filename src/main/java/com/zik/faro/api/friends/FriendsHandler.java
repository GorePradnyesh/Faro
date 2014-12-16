package com.zik.faro.api.friends;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


import java.util.ArrayList;
import java.util.List;

@Path(Constants.FRIENDS_PATH_CONST)
public class FriendsHandler {

    @Path(Constants.INVITE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> inviteFriends(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                              @QueryParam(Constants.FARO_USER_ID_PARAM) final String userId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");
        return JResponse.ok(Constants.HTTP_OK).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getFriends(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);
        final List<MinUser> friendList = new ArrayList<>();

        //TODO: Replace below static response with actual code
        friendList.add(new MinUser("David","Gilmour","dg@dgdg.com"));
        friendList.add(new MinUser("Roger","Waters","rw@dgdg.com"));

        return JResponse.ok(friendList).build();
    }

    @Path(Constants.REMOVE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> removeUser(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                           @QueryParam(Constants.FARO_USER_ID_PARAM) final String userId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");
        return JResponse.ok(Constants.HTTP_OK).build();
    }




}
