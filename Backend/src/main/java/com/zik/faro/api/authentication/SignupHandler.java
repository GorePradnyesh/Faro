package com.zik.faro.api.authentication;

import static com.zik.faro.commons.Constants.AUTH_PATH_CONST;
import static com.zik.faro.commons.Constants.AUTH_SIGN_UP_PATH_CONST;

import java.text.MessageFormat;

import javax.annotation.CheckForNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.google.gson.Gson;
import com.zik.faro.applogic.ConversionUtils;
import com.zik.faro.data.user.AppInviteStatus;
import com.zik.faro.persistence.datastore.data.user.AuthProvider;
import com.zik.faro.data.user.FaroUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
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

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


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
        String firebaseTokenString = faroSignupDetails.getFirebaseToken();

        // Validate Faro user details specified and password
        if (newFaroUser == null) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User account details missing.");
        }

        logger.info("faroUser email = " + faroSignupDetails.getFaroUser().getEmail());

        if (Strings.isNullOrEmpty(password) && Strings.isNullOrEmpty(firebaseTokenString)) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "User password/firebaseToken not specifed.");
        }

        AuthProvider authProvider = AuthProvider.FARO;
        String authProviderUserId = null;

        if (!Strings.isNullOrEmpty(firebaseTokenString)) {
            // Verify the firebase token and obtain claims
            FirebaseToken firebaseToken = FaroJwtTokenManager.verifyFirebaseToken(firebaseTokenString);

            // Find auth provider in use
            authProvider = FaroJwtTokenManager.getAuthProvider(firebaseToken);
            if (authProvider == null) {
                throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user. Auth provider could not be determined");
            }

            authProviderUserId = FaroJwtTokenManager.getAuthProviderUserId(authProvider, firebaseToken);
            if (authProviderUserId == null) {
                throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user. Auth provider user id could not be determined");
            }

            // Lookup to see if user exists with the same id and uses FARO authProvider or a different provider
            try {
                if (UserManagement.isExistingUser(newFaroUser.getEmail())) {
                    AuthProvider currentAuthProvider = UserCredentialsDatastoreImpl
                            .loadUserCreds(newFaroUser.getEmail()).getAuthProvider();

                    if (AuthProvider.FARO.equals(currentAuthProvider)) {
                        FaroUser existingfaroUser = UserManagement.loadFaroUser(newFaroUser.getEmail());

                        if (!AppInviteStatus.INVITED.equals(existingfaroUser.getInviteStatus())) {
                            // Return  error code indicating user exists
                            throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getEmail()));
                        }
                    } else if (!currentAuthProvider.equals(authProvider)) {
                        logger.info("User signup is being attempted with a different auth provider for an existing user");
                        throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getEmail()));
                    }
                }
            } catch (DataNotFoundException e) {
                throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user");
            }

        } else if (UserManagement.isExistingUser(newFaroUser.getEmail())) {
            // Lookup to sees if an user exists with the same id
            // Return  error code indicating user exists
            throw new FaroWebAppException(FaroResponseStatus.ENTITY_EXISTS, MessageFormat.format("Username {0} already exists.", newFaroUser.getEmail()));
        }

        // Create the user account in datastore and return JWT token
        createUser(newFaroUser, password, authProvider, authProviderUserId);

        return FaroJwtTokenManager.createToken(newFaroUser.getEmail());
    }

    private void createUser(FaroUser newFaroUser, String password, AuthProvider authProvider, String authProviderUserId) {
        try {
            // Store the New user's credentials and user details
            UserCredentialsDo userCreds = null;

            if (!AuthProvider.FARO.equals(authProvider)) {
                userCreds = new UserCredentialsDo(newFaroUser.getEmail(), UUID.randomUUID().toString(), authProvider, authProviderUserId);
            } else {
                userCreds = new UserCredentialsDo(newFaroUser.getEmail(), PasswordManager.getEncryptedPassword(password),
                        UUID.randomUUID().toString());
            }

            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
            FaroUserDo faroUserDo = ConversionUtils.toDo(newFaroUser);
            UserDatastoreImpl.storeUser(faroUserDo);
        } catch (PasswordManagerException e) {
            logger.error("Password could not be encrypted", e);
            throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user");
        }
    }
}
