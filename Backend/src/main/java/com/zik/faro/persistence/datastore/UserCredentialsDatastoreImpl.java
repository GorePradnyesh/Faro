package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;

/**
 * Created by granganathan on 2/15/15.
 */
public class UserCredentialsDatastoreImpl {

    public static void storeUserCreds(final UserCredentialsDo userCredentials) {
        DatastoreObjectifyDAL.storeObject(userCredentials);
    }

    public static UserCredentialsDo loadUserCreds(final String userId) throws DataNotFoundException {
        return DatastoreObjectifyDAL.loadObjectById(userId, UserCredentialsDo.class);
    }
}
