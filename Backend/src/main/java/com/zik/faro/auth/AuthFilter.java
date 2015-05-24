package com.zik.faro.auth;

import com.google.common.base.Strings;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;

/**
 * Created by granganathan on 1/28/15.
 */

/**
 * Implementation of a Jersey filter for intercepting all
 * requests and authenticating them.
 */
public class AuthFilter implements ContainerRequestFilter {
    private  static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    // all headers start with upper case
    private static  final String AUTH_HEADER = "Authentication";

    /**
     * Obtain the token from the header and validate the token.
     * Authentication information obtained about the token is passed up
     * to the resource class.
     * @param containerRequest
     * @return containerRequest
     * @throws FaroWebAppException - If the token is not present or not valid
     * @throws IllegalStateException - If token validation itself fails
     */
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        logger.info("---- Auth filter invoked. ----");

        String nativeLoginPath = Constants.AUTH_PATH_CONST + Constants.AUTH_LOGIN_PATH_CONST;
        String nativeSignupPath = Constants.AUTH_PATH_CONST + Constants.AUTH_SIGN_UP_PATH_CONST;
        String forgotPasswordPath = Constants.AUTH_PATH_CONST + Constants.AUTH_PASSWORD_PATH_CONST
                                    + Constants.AUTH_FORGOT_PASSWORD_PATH_CONST;
        String forgotPasswordFormPath = Constants.AUTH_PATH_CONST + Constants.AUTH_PASSWORD_PATH_CONST
                                         + Constants.AUTH_FORGOT_PASSWORD_FORM_PATH_CONST;
        String newPasswordPath = Constants.AUTH_PATH_CONST + Constants.AUTH_PASSWORD_PATH_CONST
                                 + Constants.AUTH_NEW_PASSWORD_PATH_CONST;
        String requestPath = "/" + containerRequest.getPath();

        logger.info("request path : " + requestPath);

        // No authentication required for login/signup requests
        if (requestPath.equals(nativeLoginPath) ||
                requestPath.equals(nativeSignupPath) ||
                requestPath.equals(forgotPasswordPath) ||
                requestPath.equals(forgotPasswordFormPath)) {
            return containerRequest;
        }

        String authHeaderValue = containerRequest.getHeaderValue(AUTH_HEADER);

        if (Strings.isNullOrEmpty(authHeaderValue)) {
            logger.error("Authentication header not present");
            throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Authentication token not provided");
        }

        logger.info("header value : " + authHeaderValue);

        try {
            // Validate the JWT token and obtain JWT claims
            // TODO: Create maven "production" and "test" profiles and
            // do complete JWT token validation only for "production" maven profile and
            // not in "test" profile
            //final FaroJwtClaims jwtClaims = FaroJwtTokenManager.obtainClaimsWithNoChecks(authHeaderValue);

            final FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(authHeaderValue);
            logger.info("jwtClaims : " + jwtClaims);

            // For new password API, check if the token has valid JWT id
            if (requestPath.equals(newPasswordPath)) {
                UserCredentials userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(jwtClaims.getUsername());
                String userCredsUuid = userCredentials.getUserCredsUUid();
                if (!userCredsUuid.equals(jwtClaims.getJwtId())) {
                    throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token");
                }
            }

            // Pass the JWT claims up to the resource classes through the SecurityContext object
            containerRequest.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return jwtClaims;
                }


                @Override
                public boolean isUserInRole(String s) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "jwt";
                }
            });
            return containerRequest;
        } catch (JwtTokenValidationException e) {
            throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token");
        } catch (SignatureException e) {
            throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "Invalid token signature");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Add logging
            throw new IllegalStateException("Unable to authenticate the request");
        }
    }
}

