package com.zik.faro.api.authentication;

import com.zik.faro.applogic.UserManagement;
import com.zik.faro.auth.PasswordManager;
import com.zik.faro.auth.PasswordManagerException;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

import static com.zik.faro.commons.Constants.*;

/**
 * Created by granganathan on 4/2/15.
 */

@Path(AUTH_PATH_CONST + AUTH_PASSWORD_PATH_CONST)
public class PasswordHandler {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHandler.class);

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @Path(AUTH_RESET_PASSWORD_PATH_CONST)
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void resetPassword(String oldPassword, String newPassword) {
        String userId = securityContext.getUserPrincipal().getName();
        // Lookup the user
        if (!UserManagement.isExistingUser(userId)) {
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                    MessageFormat.format("User {0} does not exist.", userId));
        }

        // Obtain the user credentials and authenticate the user
        UserCredentials userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(userId);

        try {
            if(!PasswordManager.checkPasswordEquality(oldPassword, userCredentials.getEncryptedPassword())) {
                throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED);
            }
        } catch (PasswordManagerException e) {
            logger.error("Unable to check the password equality", e);
            throw new IllegalStateException("Cannot verify the user credentials to reset password");
        }

        try {
            // Update the password
            UserCredentials userCreds = new UserCredentials(userId, PasswordManager.getEncryptedPassword(newPassword));
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
        } catch(PasswordManagerException e) {
            logger.error("Unable to encrypt the new password", e);
            throw new IllegalStateException("Failed to update the password");
        }
    }

    @Path(AUTH_FORGOT_PASSWORD_PATH_CONST)
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String forgotPassword(@QueryParam(FARO_USERNAME_PARAM)String userId) {
        //TODO : Return the forgotpasswordform url only via email
        // Lookup the user
        if (!UserManagement.isExistingUser(userId)) {
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                    MessageFormat.format("User {0} does not exist.", userId));
        }

        // Create a JWT token for forgot password URL
        //String jwtId = UUID.randomUUID().toString();
        //FaroJwtClaims faroJwtClaims = new FaroJwtClaims("faro", System.currentTimeMillis(),
                                                        //userId, userId);
        String token = FaroJwtTokenManager.createToken(userId);


        String forgotPasswordRequestURL = removeTrailingSlash(uriInfo.getBaseUri().toString())
                                          + AUTH_PATH_CONST
                                          + AUTH_PASSWORD_PATH_CONST
                                          + AUTH_FORGOT_PASSWORD_FORM_PATH_CONST
                                          + "?"
                                          + "token="
                                          + token;

        logger.info("uriInfo RequestUri = " + uriInfo.getRequestUri());

        return forgotPasswordRequestURL;
    }

    @Path(AUTH_FORGOT_PASSWORD_FORM_PATH_CONST)
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_HTML)
    public String forgotPasswordForm() {
        logger.info("urInfo RequestUri = " + uriInfo.getRequestUri());
        logger.info("Generating forgotpassword for m");

        try {
            return getForgotPasswordForm();
        } catch (IOException | URISyntaxException e) {
            logger.error("Could not read the html form", e);
            throw new IllegalStateException("Unable to generate forgot password form");
        }
    }

    @Path(AUTH_NEW_PASSWORD_PATH_CONST)
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void newPassword() {
        logger.info("newPassword API invoked");
    }

    private String getForgotPasswordForm() throws IOException, URISyntaxException {
        InputStream inputStream = new FileInputStream("WEB-INF/ForgotPasswordForm.html");
        if (inputStream == null) {
            logger.info("----- inputstream is Null!! ----");
        } else {
            logger.info("----- File found!! ----");
        }

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while((line = reader.readLine()) != null ) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    private String removeTrailingSlash(String input) {
        if(input.charAt(input.length() - 1) == '/') {
            return input.substring(0, input.length() - 1);
        } else {
            return input;
        }
    }
}
