package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;

import java.util.List;

public class FriendRelationDatastoreImpl {

    private static final String FROM_USER_FIELD_NAME = "fromId";

    public static void storeFriendRelation(final String faroUser1, final String faroUser2) throws IllegalDataOperation {
        /*NOTE : There are two write operations for establishing the symmetrical nature of the relationship
        The operations are **IDEMPOTENT.** because the identifier of the relationship is the combination of
        both Ids. Creating and strong a relation between two uses can be repeated any number of times without
        creating new Entities.*/
        FriendRelation friendRelation = new FriendRelation(faroUser1, faroUser2);
        DatastoreObjectifyDAL.storeObject(friendRelation);

        friendRelation = new FriendRelation(faroUser2, faroUser1);
        DatastoreObjectifyDAL.storeObject(friendRelation);
    }

    public static List<FriendRelation> loadFriendsForUserId(final String faroUserId){
        List<FriendRelation> friendRelationList =
                DatastoreObjectifyDAL.loadObjectsByAncestorRef(FaroUser.class, faroUserId, FriendRelation.class);
        return friendRelationList;
    }
}
