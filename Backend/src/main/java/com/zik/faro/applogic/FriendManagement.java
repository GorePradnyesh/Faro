package com.zik.faro.applogic;

import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.persistence.datastore.FriendRelationDatastoreImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class FriendManagement {
	
	public static void inviteFriend(final String existingUserId, final String friendId) throws DataNotFoundException, IllegalDataOperation{
		
        
        // Create friend relation between user and new friend
    	// Create relation only if it does not exist.
    	// This method is invoked by "Invite Friend API and "Add Friend To An Event API"
    	if(FriendRelationDatastoreImpl.loadFriendRelation(existingUserId,
    			friendId) == null){
    		storeFriendRelation(existingUserId,	friendId);
    	}
    }
	
	private static FaroUser getFaroUser(final String userId) throws DataNotFoundException{
    	FaroUser faroUser = UserManagement.loadFaroUser(userId);
    	return faroUser;
    }

    public static void storeFriendRelation(final String requestingUserId, final String invitedUserId) throws IllegalDataOperation, DataNotFoundException {
        List<String> userIdsToLoad = new ArrayList<>();
        userIdsToLoad.add(requestingUserId);
        userIdsToLoad.add(invitedUserId);
        Map<String, FaroUser> users = UserManagement.loadFaroUsers(userIdsToLoad);

        if(users.get(requestingUserId) ==null){
            throw new DataNotFoundException("User Not found : " + requestingUserId);
        }
        if(users.get(invitedUserId) == null){
            throw new DataNotFoundException("User Not found : " + invitedUserId);
        }

        FaroUser requestingUser = users.get(requestingUserId);
        FaroUser invitedUser = users.get(invitedUserId);

        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(requestingUser.getFirstName(), requestingUser.getLastName(), requestingUser.getEmail(), requestingUser.getExternalExpenseID()),
                new MinUser(invitedUser.getFirstName(), invitedUser.getLastName(), invitedUser.getEmail(), invitedUser.getExternalExpenseID()));
        return;
    }

    public static List<MinUser> getFriendList(final String faroUserId){
        // A valid signature implies that the faroUserId is a valid existing user. Hence no need to validate faroUserId
        List<FriendRelation> friendRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(faroUserId);
        List<MinUser> friendList = new ArrayList<>();
        for(FriendRelation friendRelation : friendRelations){
            MinUser friend = new MinUser(friendRelation.getToFName(), friendRelation.getToLName(),
                    friendRelation.getToId(), friendRelation.getToExternalExpenseId());
            friendList.add(friend);
        }
        // an empty list implies a lonely user with no friends
        return friendList;
    }
    
    public static void removeFriend(final String requestingUserId, 
    		final String toBeRemovedUserId) throws DataNotFoundException, IllegalDataOperation{
    	FaroUser faroUser1, faroUser2;
    	//TODO: No need to load the the faro user as long as remove doesnt care about non existent ids
		faroUser1 = UserManagement.loadFaroUser(requestingUserId);
		faroUser2 = UserManagement.loadFaroUser(toBeRemovedUserId);
		FriendRelationDatastoreImpl.removeFriendRelation(faroUser1, faroUser2);
	}
    

    public static void deleteFriendRelationship(final String fromUserId, final String toUserId){
        FriendRelationDatastoreImpl.deleteFriendRelation(fromUserId, toUserId);
    }

}
