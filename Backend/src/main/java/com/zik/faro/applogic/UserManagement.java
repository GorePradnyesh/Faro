package com.zik.faro.applogic;


import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.DatastoreObjectifyDAL;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import java.util.List;
import java.util.Map;

public class UserManagement {
    public static void storeFaroUser(final String userId, final FaroUserDo faroUser){
        if(faroUser.getEmail() != userId){
            throw new BadRequestException("requesting userId does not match the User to be created");
        }
        UserDatastoreImpl.storeUser(faroUser);
    }

    public static FaroUserDo loadFaroUser(final String userId) throws DataNotFoundException{
        FaroUserDo user = UserDatastoreImpl.loadFaroUserById(userId);
        return user;
    }

    public static Map<String, FaroUserDo> loadFaroUsers(List<String> objectIds){
        Map<String, FaroUserDo> users = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(objectIds, FaroUserDo.class);
        return users;
    }

    public static boolean isExistingUser(final String userId){
        try{
        	if(UserDatastoreImpl.loadFaroUserById(userId) != null) {
                return true;
            }
        }catch(DataNotFoundException e){
        	return false;
        }
    	return false;
    }
}
