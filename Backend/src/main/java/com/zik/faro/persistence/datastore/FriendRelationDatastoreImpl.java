package com.zik.faro.persistence.datastore;

import com.zik.faro.data.MinUser;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.FriendRelationDo;

import java.util.List;

public class FriendRelationDatastoreImpl {

    private static final String FROM_USER_FIELD_NAME = "fromId";

    public static void storeFriendRelation(MinUser bob, MinUser alice) throws IllegalDataOperation {
        /* Using Bob and Alice for more readability than user1/user2 or fromUser/toUser */
        /*NOTE : There are two write operations for establishing the symmetrical nature of the relationship
        The operations are **IDEMPOTENT.** because the identifier of the relationship is the combination of
        both Ids. Creating and strong a relation between two uses can be repeated any number of times without
        creating new Entities.

        We do two writes, for the symmetrical relation to save on an extra read during the load operation. Reading
        friend relations will be a lot more frequent than creating friend relations.

        Also data is de-normalized and duplicated to reduce the queries needed to load entities in the future.
        */
        FriendRelationDo friendRelation = new FriendRelationDo(bob.getEmail(), alice.getEmail(),
                                                alice.getFirstName(), alice.getLastName(), alice.getExpenseUserId());
        DatastoreObjectifyDAL.storeObject(friendRelation);

        friendRelation = new FriendRelationDo(alice.getEmail(), bob.getEmail(),
                                                bob.getFirstName(), bob.getLastName(), bob.getExpenseUserId());
        DatastoreObjectifyDAL.storeObject(friendRelation);
    }

    public static List<FriendRelationDo> loadFriendsForUserId(final String faroUserId){
        List<FriendRelationDo> friendRelationList =
                DatastoreObjectifyDAL.loadObjectsByAncestorRef(FaroUserDo.class, faroUserId, FriendRelationDo.class);
        return friendRelationList;
    }

    public static FriendRelationDo loadFriendRelation(final String faroUserId1, final String faroUserId2) throws DataNotFoundException{
        /* Because of the symmetrical nature of the relation, one could very well switch the faroUserId1,2 args to
        get the same result.*/
        FriendRelationDo relation =
                DatastoreObjectifyDAL.loadObjectWithParentId(FaroUserDo.class,
                                                                faroUserId1,
                                                                FriendRelationDo.class,
                                                                faroUserId2);
        return relation;
    }
    
    public static void removeFriendRelation(final FaroUserDo faroUser1, final FaroUserDo faroUser2) throws IllegalDataOperation{
    	
    	// Remove both sides of the relation. Similar to how we create a friend relation
    	FriendRelationDo friendRelation = new FriendRelationDo(faroUser1.getId(), faroUser2.getId(),
                faroUser2.getFirstName(), faroUser2.getLastName(), faroUser2.getExternalExpenseID());
    	DatastoreObjectifyDAL.deleteEntity(friendRelation);

    	friendRelation = new FriendRelationDo(faroUser2.getId(), faroUser1.getId(),
                faroUser1.getFirstName(), faroUser1.getLastName(), faroUser1.getExternalExpenseID());
    	DatastoreObjectifyDAL.deleteEntity(friendRelation);
    }
}
