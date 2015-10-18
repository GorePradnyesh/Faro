package com.zik.faro.api.authentication;

import static com.zik.faro.commons.Constants.AUTH_PATH_CONST;
import static com.zik.faro.commons.Constants.AUTH_SIGN_UP_PATH_CONST;

import java.text.MessageFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.text.MessageFormat;
import java.util.UUID;
import org.slf4j.Logger;

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

        // Lookup to sees if an user exists with the same id
        if (UserManagement.isExistingUser(newFaroUser.getEmail())) {
            // Return  error code indicating user exists
            logger.info("User already exists");
            throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getEmail()));
        }

        try {
            // Store the New user's credentials and user details
            UserCredentials userCreds = new UserCredentials(newFaroUser.getEmail(),
                                                            PasswordManager.getEncryptedPassword(password),
                                                            UUID.randomUUID().toString());
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
            UserDatastoreImpl.storeUser(newFaroUser);
        } catch (PasswordManagerException e) {
            logger.error("Password could not be encrypted", e);
        }

        return FaroJwtTokenManager.createToken(newFaroUser.getEmail());
    }

}
