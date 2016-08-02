package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

import java.util.List;
import java.util.Map;

public class UserDatastoreImpl {

    private static final String FIRST_NAME_FIELD_NAME = "firstName";

    public static void storeUser(final FaroUserDo user){
        DatastoreObjectifyDAL.storeObject(user);
    }

    public static FaroUserDo loadFaroUserById(final String userId) throws DataNotFoundException{
        FaroUserDo user = DatastoreObjectifyDAL.loadObjectById(userId, FaroUserDo.class);
        return user;
    }
    
    public static Map<String, FaroUserDo> loadFaroUsers(List<String> objectIds){
        Map<String, FaroUserDo> users = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(objectIds, FaroUserDo.class);
        return users;
    }

    /* Currently searches only by FirstName, for an exact match */
    public static List<FaroUserDo> loadFaroUsersByName(final String firstName){
        List<FaroUserDo> faroUsers = DatastoreObjectifyDAL.loadObjectsByIndexedStringFieldEQ(FIRST_NAME_FIELD_NAME, firstName, FaroUserDo.class);
        return faroUsers;
    }
    
    public static boolean isExistingUser(final String userId){
    	try{
        	if(loadFaroUserById(userId) != null) {
                return true;
            }
        }catch(DataNotFoundException e){
        	return false;
        }
    	return false;
    }
}
