package com.zik.faro.api.authentication;

import com.google.firebase.auth.FirebaseToken;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import com.zik.faro.persistence.datastore.data.user.AuthProvider;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.text.MessageFormat;

import static com.zik.faro.commons.Constants.AUTH_LOGIN_PATH_CONST;
import static com.zik.faro.commons.Constants.FIREBASE_AUTH_CONST;
import static com.zik.faro.commons.Constants.LOGIN_USERNAME_PARAM;

/**
 * Created by gaurav on 5/27/17.
 */
@Path(FIREBASE_AUTH_CONST)
public class FirebaseLoginHandler {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseLoginHandler.class);

    @Path(AUTH_LOGIN_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String login(@QueryParam(LOGIN_USERNAME_PARAM) final String username,
                        final String firebaseIdToken) {
        ParamValidation.genericParamValidations(username, LOGIN_USERNAME_PARAM);
        ParamValidation.genericParamValidations(firebaseIdToken, "firebaseIdToken");

        logger.info("username : " + username);

        // Authenticate the user
        try {
            // Verify the firebase token and obtain claims
            FirebaseToken firebaseToken = FaroJwtTokenManager.verifyFirebaseToken(firebaseIdToken);
            if (!username.equals(firebaseToken.getEmail())) {
                throw new FaroWebAppException(FaroResponseStatus.INVALID_LOGIN, "Invalid username");
            }

            UserCredentialsDo userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(username);

            if (!userCredentials.getAuthProvider().equals(FaroJwtTokenManager.getAuthProvider(firebaseToken))) {
                // User account is not linked with the auth provider
                throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS,
                        MessageFormat.format("Username {0} exists with email/password authentication", username));
            }

        } catch (DataNotFoundException e) {
            logger.error(MessageFormat.format("Incorrect username. User {0} does not exist", username));
            throw new FaroWebAppException(FaroResponseStatus.INVALID_LOGIN, "Invalid username");
        }

        // Generate a JWT token
        return FaroJwtTokenManager.createToken(username);
    }
}
