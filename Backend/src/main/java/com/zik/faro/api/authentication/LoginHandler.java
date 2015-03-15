package com.zik.faro.api.authentication;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.InvalidLoginException;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.zik.faro.commons.Constants.*;

/**
 * Created by granganathan on 2/3/15.
 */
@Path(AUTH_PATH_CONST)
public class LoginHandler {
    private final Logger logger = Logger.getLogger(SignupHandler.class.getName());

    /**
     * Login the user by verifying the username/password
     * and return a new JWT token
     * @param username
     * @param password
     * @return
     */
    @Path(AUTH_LOGIN_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String login(@QueryParam(LOGIN_USERNAME_PARAM) final String username,
                        final String password) {

        String idToken = null;

        ParamValidation.genericParamValidations(username, LOGIN_USERNAME_PARAM);
        ParamValidation.genericParamValidations(password, "password");

        logger.info("username : " + username);
        logger.info("password : " + password);

        // Authenticate the user
        UserCredentials userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(username);

        if (userCredentials == null) {
            // username does not match
            logger.info(MessageFormat.format("ERROR - Incorrect username. User {0} does not exist", username));
            throw new InvalidLoginException("Invalid username and/or password.");
        }

        try {
            if (!PasswordManager.checkPasswordEquality(userCredentials.getPassword(), password)) {
                logger.info("ERROR - Incorrect password");
                throw new InvalidLoginException("Invalid username and/or password.");
            }
        } catch (PasswordManagerException e) {
            e.printStackTrace();
        }

        // Generate a JWT token
        idToken = FaroJwtTokenManager.createToken(username);

        return idToken;
    }

    /**
     * Note: logout api need not be implemented at this time.
     *      Keeping the endpoint just in case it is required in the future.
     */
    @Path(AUTH_LOGOUT_PATH_CONST)
    @POST
    public void logout() {

    }
}
