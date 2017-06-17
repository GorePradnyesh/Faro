package com.zik.faro.api.authentication;

import static com.zik.faro.commons.Constants.AUTH_PATH_CONST;
import static com.zik.faro.commons.Constants.AUTH_SIGN_UP_PATH_CONST;
import static com.zik.faro.commons.Constants.FARO_USER_ID_PARAM;

import java.text.MessageFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.zik.faro.applogic.ConversionUtils;
import com.zik.faro.data.user.FaroUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.sun.jersey.api.JResponse;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.auth.PasswordManager;
import com.zik.faro.auth.PasswordManagerException;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;
import java.text.MessageFormat;
import java.util.UUID;


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

        logger.info("faroUser email = " + faroSignupDetails.getFaroUser().getId());

        if (Strings.isNullOrEmpty(password)) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User password not specifed.");
        }

        // Lookup to sees if an user exists with the same id
        if (UserManagement.isExistingUser(newFaroUser.getId())) {
            // Return  error code indicating user exists
            logger.info("User already exists");
            throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getId()));
        }

        try {
        	// Store the New user's credentials and user details
            UserCredentialsDo userCreds = new UserCredentialsDo(newFaroUser.getId(),
                                                            PasswordManager.getEncryptedPassword(password),
                                                            UUID.randomUUID().toString());
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
            FaroUserDo faroUserDo = ConversionUtils.toDo(newFaroUser);
            UserDatastoreImpl.storeUser(faroUserDo);
        } catch (PasswordManagerException e) {
            logger.error("Password could not be encrypted", e);
        }

        return FaroJwtTokenManager.createToken(newFaroUser.getId());
    }

}
