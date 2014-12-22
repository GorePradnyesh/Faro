package com.zik.faro.applogic;


import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

public class UserManagement {
    public static void storeFaroUser(final String userId, final FaroUser faroUser){
        // TODO: Validate FaroUser parameters
        if(faroUser.getId() != userId){
            throw new BadRequestException("requesting userId does not match the User to be created");
        }
        UserDatastoreImpl.storeUser(faroUser);
        //TODO: Store the user name and password mapping in another "table".
    }

    public static FaroUser loadFaroUser(final String userId){
        FaroUser user = UserDatastoreImpl.loadFaroUserById(userId);
        //TODO: Add logging
        return user;
    }
}
