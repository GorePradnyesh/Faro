package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

import java.util.List;

public class UserDatastoreImpl {

    private static final String FIRST_NAME_FIELD_NAME = "firstName";

    public static void storeUser(final FaroUserDo user){
        DatastoreObjectifyDAL.storeObject(user);
    }

    public static FaroUserDo loadFaroUserById(final String userId) throws DataNotFoundException{
        FaroUserDo user = DatastoreObjectifyDAL.loadObjectById(userId, FaroUserDo.class);
        return user;
    }

    /* Currently searches only by FirstName, for an exact match */
    public static List<FaroUserDo> loadFaroUsersByName(final String firstName){
        List<FaroUserDo> faroUsers = DatastoreObjectifyDAL.loadObjectsByIndexedStringFieldEQ(FIRST_NAME_FIELD_NAME, firstName, FaroUserDo.class);
        return faroUsers;
    }
}
