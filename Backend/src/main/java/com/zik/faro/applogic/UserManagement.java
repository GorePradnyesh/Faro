package com.zik.faro.applogic;


import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.DatastoreObjectifyDAL;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import java.util.List;
import java.util.Map;

public class UserManagement {
    public static void storeFaroUser(final String userId, final FaroUser faroUser){
        // TODO: Validate FaroUser parameters
        if(faroUser.getId() != userId){
            throw new BadRequestException("requesting userId does not match the User to be created");
        }
        UserDatastoreImpl.storeUser(faroUser);
    }

    public static FaroUser loadFaroUser(final String userId) throws DataNotFoundException{
        FaroUser user = UserDatastoreImpl.loadFaroUserById(userId);
        return user;
    }

    public static Map<String, FaroUser> loadFaroUsers(List<String> objectIds){
        Map<String, FaroUser> users = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(objectIds, FaroUser.class);
        return users;
    }

    public static boolean isExistingUser(final String userId) throws DataNotFoundException {
        if(UserDatastoreImpl.loadFaroUserById(userId) != null) {
            return true;
        }

        return false;
    }
}
