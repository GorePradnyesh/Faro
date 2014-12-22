package com.zik.faro.applogic;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.persistence.datastore.FriendRelationDatastoreImpl;

import java.util.List;

public class FriendManagement {

    public static void inviteFriend(final String requestingUserId, final String invitedUserId) throws IllegalDataOperation, DataNotFoundException {
        //Validate that the invitedUserID is a valid userID
        FaroUser invitedUser = UserManagement.loadFaroUser(invitedUserId);
        if(invitedUser == null){
            throw new DataNotFoundException("Invited user not found : " + invitedUserId);
        }

        FriendRelationDatastoreImpl.storeFriendRelation(requestingUserId, invitedUserId);
        return;
    }

    public static List<FriendRelation> getFriendList(final String faroUserId){
        // A valid signature implies that the faroUserId is a valid existing user. Hence no need to validate faroUserId
        List<FriendRelation> friendRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(faroUserId);
        return friendRelations;
    }

}
