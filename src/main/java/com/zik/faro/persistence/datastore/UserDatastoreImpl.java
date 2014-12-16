package com.zik.faro.persistence.datastore;

import com.zik.faro.data.user.FaroUser;

import java.util.List;

public class UserDatastoreImpl {

    private static final String FIRST_NAME_FIELD_NAME = "firstName";

    public static void storeFaroUser(final FaroUser user){
        DatastoreObjectifyDAL.storeObject(user);
    }

    public static FaroUser loadFaroUserById(final String userId){
        FaroUser user = DatastoreObjectifyDAL.loadObjectById(userId, FaroUser.class);
        return user;
    }

    public static List<FaroUser> loadFaroUsersByName(final String firstName){
        List<FaroUser> faroUsers = DatastoreObjectifyDAL.loadObjectsByIndexedFieldEQ(FIRST_NAME_FIELD_NAME, firstName, FaroUser.class);
        return faroUsers;
    }

    //TODO: Implement search user by name.
}
