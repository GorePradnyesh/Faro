package com.zik.faro.persistence.datastore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;

/**
 * Created by granganathan on 2/15/15.
 */
public class UserCredentialsDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(UserCredentialsDatastoreImpl.class);
    
	public static void storeUserCreds(UserCredentialsDo userCredentials) {
        DatastoreObjectifyDAL.storeObject(userCredentials);
        logger.info("User Credentials created");
    }

    public static UserCredentialsDo loadUserCreds(String userId) throws DataNotFoundException {
        UserCredentialsDo userCredentialsDo = DatastoreObjectifyDAL.loadObjectById(userId, UserCredentialsDo.class);
        logger.info("User Credentials loaded");
        return userCredentialsDo;
    }

    public static UserCredentialsDo loadUserCredsByAuthProviderUserId(String authProviderUserId) throws DataNotFoundException {
    	UserCredentialsDo userCredentialDo = DatastoreObjectifyDAL.loadFirstObjectByIndexedStringFieldEQ("authProviderUserId", authProviderUserId, UserCredentialsDo.class);
    	logger.info("User Credentials loaded given authProviderUserId");
    	return userCredentialDo;
    }
}
