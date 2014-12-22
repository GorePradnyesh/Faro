package com.zik.faro.applogic;

import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.persistence.datastore.FriendRelationDatastoreImpl;

import java.util.List;

public class FriendManagement {
    public static void inviteFriend(final String requestingUserId, final String inviteeUserId) throws IllegalDataOperation {
        FriendRelationDatastoreImpl.storeFriendRelation(requestingUserId, inviteeUserId);
        return;
    }

    public static List<FriendRelation> getFriendList(final String faroUserId){
        // A valid signature implies that the faroUserId is a valid existing user. Hence no need to validate faroUserId
        List<FriendRelation> friendRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(faroUserId);
        return friendRelations;
    }

}
