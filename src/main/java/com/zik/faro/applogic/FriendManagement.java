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

public class FriendManagement {
	
	public static void inviteFriend(final String existingUserId, final String friendId){
		FaroUser existingUser = UserManagement.loadFaroUser(existingUserId);
    	
		// Invite friend to app if not already there.
        FaroUser invitedFaroUser = getFaroUser(friendId); 	
        
        // Create friend relation between user and new friend
        try {
        	// Create relation only if it does not exist.
        	// This method is invoked by "Invite Friend API and "Add Friend To An Event API"
        	if(FriendRelationDatastoreImpl.loadFriendRelation(existingUser.getId(),
        			invitedFaroUser.getId()) == null){
        		storeFriendRelation(existingUser.getId(), invitedFaroUser.getId());
        	}
        } catch (IllegalDataOperation e) {
				// TODO
		} catch (DataNotFoundException e) {
				// TODO
		}
    }
	
	private static FaroUser getFaroUser(final String userId){
    	FaroUser faroUser = UserManagement.loadFaroUser(userId);
    	if(faroUser == null){
    		// TODO: Send out web invite
    		
    		// Create new faroUser. This has to be later updated with all other details
    		// once user agrees to create a profile and accepts the web request to join
    		faroUser = new FaroUser(userId);
    		UserManagement.storeFaroUser(userId,faroUser);
    	}
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
    		final String toBeRemovedUserId){
    	FaroUser faroUser1 = UserManagement.loadFaroUser(requestingUserId);
    	FaroUser faroUser2 = UserManagement.loadFaroUser(toBeRemovedUserId);
    	
    	if(faroUser1 == null || faroUser2 == null){
    		//TODO: What to do here if one of the users dont exist. Simply return?
    		return;
    	}
    	try {
			FriendRelationDatastoreImpl.removeFriendRelation(faroUser1, faroUser2);
		} catch (IllegalDataOperation e) {
			// TODO:
		}
    }
    

}
