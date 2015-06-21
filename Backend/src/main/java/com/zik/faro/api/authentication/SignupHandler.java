package com.zik.faro.api.authentication;

import com.google.common.base.Strings;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.auth.PasswordManager;
import com.zik.faro.auth.PasswordManagerException;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.text.MessageFormat;

import org.slf4j.Logger;

import static com.zik.faro.commons.Constants.*;


/**
 * Created by granganathan on 2/8/15.
 */
@Path(AUTH_PATH_CONST + AUTH_SIGN_UP_PATH_CONST)
public class SignupHandler {
    private static Logger logger = LoggerFactory.getLogger(SignupHandler.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String signupUser(FaroSignupDetails faroSignupDetails) {
        if (faroSignupDetails == null) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User signup  details missing.");
        }

        logger.info("faro user = " + faroSignupDetails.getFaroUser());

        FaroUser newFaroUser = faroSignupDetails.getFaroUser();
        String password = faroSignupDetails.getPassword();

        // Validate Faro user details specified and password
        if (newFaroUser == null) {
            logger.info("User account details missing");
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User account details missing.");
        }

        logger.info("faroUser email = " + faroSignupDetails.getFaroUser().getEmail());

        if (Strings.isNullOrEmpty(password)) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User password not specifed.");
        }

        

        try {
        	UserManagement.isExistingUser(newFaroUser.getId());
            // Store the New user's credentials and user details
            UserCredentials userCreds = new UserCredentials(newFaroUser.getEmail(),
                                                            PasswordManager.getEncryptedPassword(password));
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
            UserDatastoreImpl.storeUser(newFaroUser);
        } catch (PasswordManagerException e) {
            logger.error("Password could not be encrypted", e);
        } catch (DataNotFoundException e) {
        	// Return  error code indicating user exists
            logger.info("User already exists");
            // TODO (Code Review) : throw only WebApplicationException . Keep an emum of Faro status codes
            throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getEmail()));
       
		}

        return FaroJwtTokenManager.createToken(newFaroUser.getId());
    }

}