package com.zik.faro.applogic;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.notifications.handler.UserNotificationHandler;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class UserManagement {
    
	public static UserNotificationHandler userNotificationHandler = new UserNotificationHandler();
	
	public static void storeFaroUser(final String userId, final FaroUser faroUser){
        if(faroUser.getId() != userId){
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
    
    public static FaroUser upsertFaroUser(final FaroUser updateObj, final String faroUserId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	try{
    		FaroUserDo user = UserDatastoreImpl.loadFaroUserById(faroUserId);
    	}catch(DataNotFoundException e){
    		// Insert
    		storeFaroUser(faroUserId, updateObj);
    		return updateObj;
    	}
    	return updateFaroUser(updateObj, faroUserId);
    }
    
    public static void addRegistrationToken(final String faroUserId, final String registrationToken) throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	FaroUserDo user = UserDatastoreImpl.loadFaroUserById(faroUserId);
		user.getTokens().add(registrationToken);
		UserDatastoreImpl.updateFaroUser(faroUserId, user);
		try {
			userNotificationHandler.subscribeToTopic(faroUserId, user.getTokens().toArray(new String[0]));
		} catch (FirebaseNotificationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void removeRegistrationToken(final String faroUserId, final String registrationToken) throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	FaroUserDo user = UserDatastoreImpl.loadFaroUserById(faroUserId);
		user.getTokens().remove(registrationToken);
		UserDatastoreImpl.updateFaroUser(faroUserId, user);
    }
    
    public static FaroUser updateFaroUser(final FaroUser updateObj, final String faroUserId) throws DataNotFoundException, DatastoreException, UpdateVersionException {
        FaroUserDo updateObjDo = ConversionUtils.toDo(updateObj);
    	return ConversionUtils.fromDo(UserDatastoreImpl.updateFaroUser(faroUserId, updateObjDo));
    }
    

    public static boolean isExistingUser(final String userId){
        return UserDatastoreImpl.isExistingUser(userId);
    }
}
