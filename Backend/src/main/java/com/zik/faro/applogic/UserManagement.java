package com.zik.faro.applogic;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class UserManagement {
    public static void storeFaroUser(final String userId, final FaroUser faroUser){
        if(faroUser.getEmail() != userId){
            throw new BadRequestException("requesting userId does not match the User to be created");
        }
        
        UserDatastoreImpl.storeUser(ConversionUtils.toDo(faroUser));
    }

    public static FaroUser loadFaroUser(final String userId) throws DataNotFoundException{
        FaroUserDo user = UserDatastoreImpl.loadFaroUserById(userId);
        return ConversionUtils.fromDo(user);
    }

    public static Map<String, FaroUser> loadFaroUsers(List<String> objectIds){
        Map<String, FaroUserDo> userDos = UserDatastoreImpl.loadFaroUsers(objectIds);
        Map<String, FaroUser> users = new HashMap<String, FaroUser>();
        for(String objectId: objectIds){
        	if(userDos.containsKey(objectId)){
        		users.put(objectId, ConversionUtils.fromDo(userDos.get(objectId)));
        	}else{
        		users.put(objectId, null);
        	}
        }
        return users;
    }

    public static boolean isExistingUser(final String userId){
        return UserDatastoreImpl.isExistingUser(userId);
    }
}
