package com.zik.faro.api.friends;

import com.sun.jersey.api.JResponse;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.MinUser;
import com.zik.faro.applogic.FriendManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import com.zik.faro.persistence.datastore.data.user.FriendRelationDo;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.text.MessageFormat;
import java.util.List;

@Path(Constants.FRIENDS_PATH_CONST)
public class FriendsHandler {
    private static Logger logger = LoggerFactory.getLogger(FriendsHandler.class);

    @Context
    SecurityContext securityContext;

    @Path(Constants.INVITE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> inviteFriend(String friendId) {
        //ParamValidation.genericParamValidations(friendId, "userId");

        String requestingUserId = securityContext.getUserPrincipal().getName();
       
        try {
			FriendManagement.createFriendRelation(requestingUserId, friendId);
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

    @Path(Constants.FACEBOOK_FRIENDS_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> inviteFbFriends(List<String> fbUserIds) {

        String requestingUserId = securityContext.getUserPrincipal().getName();

        if (fbUserIds.isEmpty()) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "facebook friends user ids missing");
        }

        List<MinUser> friends = Lists.newArrayList();
        for (String fbUserId : fbUserIds) {
            try {
                // Find friend using their authprovider user id
                UserCredentialsDo userCredentialsDo = UserCredentialsDatastoreImpl.loadUserCredsByAuthProviderUserId(fbUserId);
                String friendId = userCredentialsDo.getEmail();

                logger.info(MessageFormat.format("Found fb friend {0} with fb user id {1}", friendId, fbUserId));

                // Create friend relation
                FriendRelationDo friendRelationDo = FriendManagement.createFriendRelation(requestingUserId, friendId);

                // Add friend as Minuser in response list
                friends.add(new MinUser(friendRelationDo.getToFName(), friendRelationDo.getToLName(), friendId));

                logger.info(MessageFormat.format("Created friend relation with fb friend {0}", friendId));
            } catch (DataNotFoundException e) {
                logger.error(MessageFormat.format("Could not find facebook friend with authProvider user id {0}. " +
                        "Friend relationship could not be created with user {1}", fbUserId, requestingUserId), e);
                throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Could not find facebook friend with authProvider user id");
            } catch (IllegalDataOperation e) {
                throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, e.getMessage());
            }
        }

        return JResponse.ok(friends).build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getFriends() {
        String userId = securityContext.getUserPrincipal().getName();
        List<MinUser> friends = FriendManagement.getFriendList(userId);

        return JResponse.ok(friends).build();
    }

    @Path(Constants.REMOVE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    public JResponse<String> unFriend(@QueryParam(Constants.FARO_USER_ID_PARAM) final String toBeRemovedUserId) {
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
