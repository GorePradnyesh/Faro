package com.zik.faro.api.authentication;

import static com.zik.faro.commons.Constants.AUTH_LOGIN_PATH_CONST;
import static com.zik.faro.commons.Constants.AUTH_LOGOUT_PATH_CONST;
import static com.zik.faro.commons.Constants.AUTH_PATH_CONST;
import static com.zik.faro.commons.Constants.LOGIN_USERNAME_PARAM;

import java.text.MessageFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.JResponse;
import com.zik.faro.auth.PasswordManager;
import com.zik.faro.auth.PasswordManagerException;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;

/**
 * Created by granganathan on 2/3/15.
 */
@Path(AUTH_PATH_CONST)
public class LoginHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    /**
     * Login the user by verifying the username/password
     * and return a new JWT token
     * @param username
     * @param password
     * @return
     */
    @Path(AUTH_LOGIN_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<String> login(@QueryParam(LOGIN_USERNAME_PARAM) final String username,
                        final String password) {
        ParamValidation.genericParamValidations(username, LOGIN_USERNAME_PARAM);
        ParamValidation.genericParamValidations(password, "password");

        logger.info("username : " + username);
        logger.info("password : " + password);

        try {
        	UserCredentialsDo userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(username);

            if (!PasswordManager.checkPasswordEquality(password, userCredentials.getEncryptedPassword())) {
                logger.error("Incorrect password");
                throw new FaroWebAppException(FaroResponseStatus.INVALID_LOGIN, "Invalid username and/or password.");
            }
        } catch (PasswordManagerException e) {
            logger.error("Could not verify password.", e);
            throw new IllegalStateException("Unable to authenticate the user.");
        } catch (DataNotFoundException e) {
        	// username does not match
            logger.error(MessageFormat.format("Incorrect username. User {0} does not exist", username));
            throw new FaroWebAppException(FaroResponseStatus.INVALID_LOGIN, "Invalid username and/or password.");
		}

        // Generate a JWT token
        return JResponse.ok(FaroJwtTokenManager.createToken(username)).build();
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
