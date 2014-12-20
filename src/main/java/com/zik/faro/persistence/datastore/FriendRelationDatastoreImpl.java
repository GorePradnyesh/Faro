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
        creating new Entities.

        We do two writes, for the symmetrical relation to save on an extra read during the load operation. Reading
        friend relations will be a lot more frequent than creating friend relations.
        */
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

    public static FriendRelation loadFriendRelation(final String faroUserId1, final String faroUserId2){
        /* Because of the symmetrical nature of the relation, one could very well switch the faroUserId1,2 args to
        get the same result.*/
        FriendRelation relation =
                DatastoreObjectifyDAL.loadObjectWithParentId(FaroUser.class,
                                                                faroUserId1,
                                                                FriendRelation.class,
                                                                faroUserId2);
        return relation;
    }
}
