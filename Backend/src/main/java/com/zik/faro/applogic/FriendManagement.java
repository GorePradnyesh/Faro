package com.zik.faro.applogic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.FriendRelationDatastoreImpl;
import com.zik.faro.persistence.datastore.data.user.FriendRelationDo;
import org.apache.log4j.Logger;

public class FriendManagement {
    private static final Logger logger = Logger.getLogger(FriendManagement.class);
	
	public static FriendRelationDo createFriendRelation(final String existingUserId, final String friendId) throws IllegalDataOperation, DataNotFoundException {
        // Create friend relation between user and new friend
    	// Create relation only if it does not exist.
    	// This method is invoked by "Invite Friend API and "Add Friend To An Event API"
    	try {
			return FriendRelationDatastoreImpl.loadFriendRelation(existingUserId,friendId);
		} catch (DataNotFoundException e) {
			// Either friend not a FaroUser or not user's friend. 
			if (!UserManagement.isExistingUser(friendId)) {
				// Create basic user with email and inviteStatus as INVITED(default)
				FaroUser friend = new FaroUser();
				friend.setId(friendId);
				UserManagement.storeFaroUser(friendId, friend);
				//TODO: Send out email
			}

			storeFriendRelation(existingUserId,	friendId);
            return FriendRelationDatastoreImpl.loadFriendRelation(existingUserId,friendId);
		} 
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
                new MinUser(requestingUser.getFirstName(), requestingUser.getLastName(), requestingUser.getId(), requestingUser.getExternalExpenseID()),
                new MinUser(invitedUser.getFirstName(), invitedUser.getLastName(), invitedUser.getId(), invitedUser.getExternalExpenseID()));
        return;
    }

    public static List<MinUser> getFriendList(final String faroUserId) {
        // A valid signature implies that the faroUserId is a valid existing user. Hence no need to validate faroUserId
        List<FriendRelationDo> friendRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(faroUserId);
        List<MinUser> friendList = new ArrayList<>();
        for(FriendRelationDo friendRelation : friendRelations) {
            FaroUser friendUser = null;
            try {
                friendUser = UserManagement.loadFaroUser(friendRelation.getToId());
            } catch (DataNotFoundException e) {
                logger.info(MessageFormat.format("Cannot get display picture since friend {0} is not yet a registered faro user", friendRelation.getToId()));
            }

            MinUser friend = new MinUser()
                    .withFirstName(friendRelation.getToFName())
                    .withLastName(friendRelation.getToLName())
                    .withEmail(friendRelation.getToId())
                    .withExpenseUserId(friendRelation.getToExternalExpenseId())
                    .withProfileImageUrl(friendUser.getLargeProfileImage() != null ?
                            friendUser.getLargeProfileImage().getPublicUrl().toString() : null)
                    .withThumbProfileImageUrl(friendUser.getSmallProfileImage() != null ?
                            friendUser.getSmallProfileImage().getPublicUrl().toString() : null);

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
		FriendRelationDatastoreImpl.removeFriendRelation(ConversionUtils.toDo(faroUser1), ConversionUtils.toDo(faroUser2));
	}
}
