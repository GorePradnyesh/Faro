package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.MinUser;
import com.zik.faro.persistence.datastore.FriendRelationDatastoreImpl;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.FriendRelationDo;

public class FriendManagement {
	
	public static void createFriendRelation(final String existingUserId, final String friendId) throws IllegalDataOperation, DataNotFoundException{
		
        
        // Create friend relation between user and new friend
    	// Create relation only if it does not exist.
    	// This method is invoked by "Invite Friend API and "Add Friend To An Event API"
    	try {
			FriendRelationDatastoreImpl.loadFriendRelation(existingUserId,friendId);
		} catch (DataNotFoundException e) {
			// Either friend not a FaroUser or not user's friend. 
			if(!UserManagement.isExistingUser(friendId)){
				// Create basic user with email and inviteStatus as INVITED(default)
				UserManagement.storeFaroUser(friendId, new FaroUserDo(friendId));
				//TODO: Send out email
			}
			storeFriendRelation(existingUserId,	friendId);
		} 
    }
	

    public static void storeFriendRelation(final String requestingUserId, final String invitedUserId) throws IllegalDataOperation, DataNotFoundException {
        List<String> userIdsToLoad = new ArrayList<>();
        userIdsToLoad.add(requestingUserId);
        userIdsToLoad.add(invitedUserId);
        Map<String, FaroUserDo> users = UserManagement.loadFaroUsers(userIdsToLoad);

        if(users.get(requestingUserId) ==null){
            throw new DataNotFoundException("User Not found : " + requestingUserId);
        }
        if(users.get(invitedUserId) == null){
            throw new DataNotFoundException("User Not found : " + invitedUserId);
        }

        FaroUserDo requestingUser = users.get(requestingUserId);
        FaroUserDo invitedUser = users.get(invitedUserId);

        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(requestingUser.getFirstName(), requestingUser.getLastName(), requestingUser.getEmail(), requestingUser.getExternalExpenseID()),
                new MinUser(invitedUser.getFirstName(), invitedUser.getLastName(), invitedUser.getEmail(), invitedUser.getExternalExpenseID()));
        return;
    }

    public static List<MinUser> getFriendList(final String faroUserId){
        // A valid signature implies that the faroUserId is a valid existing user. Hence no need to validate faroUserId
        List<FriendRelationDo> friendRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(faroUserId);
        List<MinUser> friendList = new ArrayList<>();
        for(FriendRelationDo friendRelation : friendRelations){
            MinUser friend = new MinUser(friendRelation.getToFName(), friendRelation.getToLName(),
                    friendRelation.getToId(), friendRelation.getToExternalExpenseId());
            friendList.add(friend);
        }
        // an empty list implies a lonely user with no friends
        return friendList;
    }
    
    public static void removeFriend(final String requestingUserId, 
    		final String toBeRemovedUserId) throws DataNotFoundException, IllegalDataOperation{
    	FaroUserDo faroUser1, faroUser2;
    	//TODO: No need to load the the faro user as long as remove doesnt care about non existent ids
		faroUser1 = UserManagement.loadFaroUser(requestingUserId);
		faroUser2 = UserManagement.loadFaroUser(toBeRemovedUserId);
		FriendRelationDatastoreImpl.removeFriendRelation(faroUser1, faroUser2);
	}
    

//    public static void deleteFriendRelationship(final String fromUserId, final String toUserId){
//        FriendRelationDatastoreImpl.removeFriendRelation(fromUserId, toUserId);
//    }

}
