package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.user.UserCredentials;

import javax.jws.soap.SOAPBinding;

/**
 * Created by granganathan on 2/15/15.
 */
public class UserCredentialsDatastoreImpl {

    public static void storeUserCreds(final UserCredentials userCredentials) {
        DatastoreObjectifyDAL.storeObject(userCredentials);
    }

    public static UserCredentials loadUserCreds(final String userId) throws DataNotFoundException {
        return DatastoreObjectifyDAL.loadObjectById(userId, UserCredentials.class);
    }
}
