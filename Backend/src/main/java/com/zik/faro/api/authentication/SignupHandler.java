package com.zik.faro.api.authentication;

import com.google.common.base.Strings;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.EntityAlreadyExistsException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import java.text.MessageFormat;
import java.util.logging.Logger;

import static com.zik.faro.commons.Constants.*;

/**
 * Created by granganathan on 2/8/15.
 */
@Path(AUTH_PATH_CONST + AUTH_SIGN_UP_PATH_CONST)
public class SignupHandler {
    private final Logger logger = Logger.getLogger(SignupHandler.class.getName());

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response signupUser(FaroSignupDetails faroSignupDetails) {
        if (faroSignupDetails == null) {
            throw new BadRequestException("User signup  details missing.");
        }

        logger.info("faro user = " + faroSignupDetails.getFaroUser());
        logger.info("faroUser email = " + faroSignupDetails.getFaroUser().getEmail());

        FaroUser newFaroUser = faroSignupDetails.getFaroUser();
        String password = faroSignupDetails.getPassword();

        // Validate Faro user details specified and password
        if (newFaroUser == null) {
            logger.info("User account details missing");
            throw new BadRequestException("User account details missing.");
        }

        if (Strings.isNullOrEmpty(password)) {
            throw new BadRequestException("User password not specifed.");
        }

        // Lookup to sees if an user exists with the same id
        if (UserManagement.isExistingUser(newFaroUser.getId())) {
            // Return  error code indicating user exists
            throw new EntityAlreadyExistsException(MessageFormat.format("User {0} already exists.", newFaroUser.getEmail()));
        }

        try {
            // Store the New user's credentials and user details
            UserCredentials userCreds = new UserCredentials(newFaroUser.getEmail(),
                                                            PasswordManager.getEncryptedPassword(password));
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
            UserDatastoreImpl.storeUser(newFaroUser);
        } catch (PasswordManagerException e) {
            logger.warning("ERROR: Password could not be encrypted");
        }

        //TODO : Generate and Send Welcome email

        return Response.ok().build();
    }

}
