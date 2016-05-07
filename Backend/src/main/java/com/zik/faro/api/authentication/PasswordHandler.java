 package com.zik.faro.api.authentication;

import com.google.common.base.Strings;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.auth.PasswordManager;
import com.zik.faro.auth.PasswordManagerException;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.FaroResetPasswordData;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.zik.faro.commons.Constants.*;

/**
 * Created by granganathan on 4/2/15.
 */

@Path(AUTH_PATH_CONST + AUTH_PASSWORD_PATH_CONST)
public class PasswordHandler {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHandler.class);
    private static final String FORGOT_PASSWORD_HTML_PAGE = "WEB-INF/ForgotPasswordForm.html";

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    /**
     * Reset password for a logged in user
     * @param resetPasswordData
     */
    @Path(AUTH_RESET_PASSWORD_PATH_CONST)
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void resetPassword(FaroResetPasswordData resetPasswordData) {
        String userId = securityContext.getUserPrincipal().getName();
        // Lookup the user
        if (!UserManagement.isExistingUser(userId)) {
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                    MessageFormat.format("User {0} does not exist.", userId));
        }

        try {
            // Obtain the user credentials and authenticate the user
            UserCredentialsDo userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(userId);

            if(!PasswordManager.checkPasswordEquality(resetPasswordData.getOldPassword(), userCredentials.getEncryptedPassword())) {
                throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED);
            }
        } catch (PasswordManagerException e) {
            logger.error("Unable to check the password equality", e);
            throw new IllegalStateException("Cannot verify the user credentials to reset password");
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }

        updatePassword(userId, resetPasswordData.getNewPassword());
    }

    /**
     * Generate and return URL for changing the password for this user
     * @param userId
     * @return
     */
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

        UserCredentialsDo userCredentials = null;
        try {
            userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(userId);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        if (userCredentials == null) {
            logger.error(MessageFormat.format("Usercredentials could not be obtained for user {0}", userId));
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                    MessageFormat.format("User {0} does not exist.", userId));
        }

        String userCredsUuid = userCredentials.getUserCredsUUid();

        // Create a JWT token for forgot password URL
        // including the user creds uuid as the JWT id and a 24 hour expiration
        long currentTimeInMillisecs = System.currentTimeMillis();
        long expirationTimeInSecs = currentTimeInMillisecs/1000L + TimeUnit.HOURS.toSeconds(24);
        FaroJwtClaims faroJwtClaims = new FaroJwtClaims("faro", currentTimeInMillisecs,
                                                        userId, userId, userCredsUuid);

        faroJwtClaims.setExpirationInSecs(expirationTimeInSecs);
        String token = FaroJwtTokenManager.createToken(faroJwtClaims);

        logger.info("jwt claims of token in URL: " + faroJwtClaims);

        // Create the forgot password form URL with the token in the query string
        String  forgotPasswordFormUrl = new StringBuilder()
                .append(removeTrailingSlash(uriInfo.getBaseUri().toString()))
                .append(AUTH_PATH_CONST)
                .append(AUTH_PASSWORD_PATH_CONST)
                .append(AUTH_FORGOT_PASSWORD_FORM_PATH_CONST)
                .append("?")
                .append("token=")
                .append(token)
                .toString();

        return forgotPasswordFormUrl;
    }

    /**
     * Return HTML page for entering the new password
     * @param token
     * @return
     */
    @Path(AUTH_FORGOT_PASSWORD_FORM_PATH_CONST)
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_HTML)
    public String forgotPasswordForm(@QueryParam(FARO_TOKEN_PARAM)String token) {
        FaroJwtClaims jwtClaims = null;
        try {
            // Authenticate the request first
            jwtClaims = FaroJwtTokenManager.validateToken(token);
            logger.info("jwtClaims : " + jwtClaims);
            // Lookup the user
            if (!UserManagement.isExistingUser(jwtClaims.getUsername())) {
                throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                        MessageFormat.format("User {0} does not exist.", jwtClaims.getUsername()));
            }
            String userCredsUuid = UserCredentialsDatastoreImpl.loadUserCreds(jwtClaims.getUsername()).getUserCredsUUid();
            if (!userCredsUuid.equals(jwtClaims.getJwtId())) {
                logger.error(MessageFormat.format("jwt id {0} is invalid. userCredsUuid = {1}",
                        jwtClaims.getJwtId(), userCredsUuid));
                throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token to change password");
            }

            // return the html page
            logger.info("Generating forgot password form");
            return getForgotPasswordForm();
        } catch (JwtTokenValidationException e) {
            throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token");
        } catch (SignatureException e) {
            throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token signature");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Unable to validate the token", e);
            throw new IllegalStateException("Unable to authenticate the request to change password");
        } catch (URISyntaxException | IOException e) {
            logger.error("Failed to generate forgot password html page", e);
            throw new IllegalStateException("Unable to generate forgot password form");
        } catch (DataNotFoundException e) {
            throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND,
                    MessageFormat.format("User {0} does not exist.", jwtClaims.getUsername()));
        }
    }

    /**
     * Invoked from the forgot password page to update the password
     * @param newPassword
     */
    @Path(AUTH_NEW_PASSWORD_PATH_CONST)
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void newPassword(String newPassword) {
        // No need to authenticate the request as this API request would have been authenticated
        // by the auth filter by verifying the token
        logger.info("newPassword API invoked ....");
        if (Strings.isNullOrEmpty(newPassword)) {
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "Password is null or empty");
        }

        String userId = securityContext.getUserPrincipal().getName();
        logger.info(MessageFormat.format("Updating the password of user {0}, password = {1}", userId, newPassword));

        updatePassword(userId, newPassword);
    }

    /**
     * Update the password
     * @param userId
     * @param newPassword
     */
    private void updatePassword(String userId, String newPassword) {
        try {
            // Update the password
            UserCredentialsDo userCreds = new UserCredentialsDo(userId, PasswordManager.getEncryptedPassword(newPassword), UUID.randomUUID().toString());
            UserCredentialsDatastoreImpl.storeUserCreds(userCreds);
        } catch(PasswordManagerException e) {
            logger.error("Unable to encrypt the new password", e);
            throw new IllegalStateException("Failed to update the password");
        }
    }

    /**
     * Return the forgot password html page as a string
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private String getForgotPasswordForm() throws IOException, URISyntaxException {
        InputStream inputStream = getHtmlFormAsStream();
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

    /**
     * Return the InputStream for the html form
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getHtmlFormAsStream() throws FileNotFoundException {
        Properties properties = System.getProperties();
        String testProp = properties.getProperty("unit-test");

        if("true".equals(testProp)) {
            return ClassLoader.getSystemResourceAsStream(FORGOT_PASSWORD_HTML_PAGE);
        }

        return new FileInputStream(FORGOT_PASSWORD_HTML_PAGE);
    }

    /**
     * Remove '/' at the end of the path if it exists
     * @param input
     * @return
     */
    private String removeTrailingSlash(String input) {
        if(input.charAt(input.length() - 1) == '/') {
            return input.substring(0, input.length() - 1);
        } else {
            return input;
        }
    }
}
