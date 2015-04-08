package com.zik.faro.auth;

import com.google.common.base.Strings;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.UnauthorizedException;
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
     * @throws UnauthorizedException - If the token is not present or not valid
     * @throws IllegalStateException - If token validation itself fails
     */
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        logger.info("---- Auth filter invoked. ----");

        String nativeLoginPath = Constants.AUTH_PATH_CONST + Constants.AUTH_LOGIN_PATH_CONST;
        String nativeSignupPath = Constants.AUTH_PATH_CONST + Constants.AUTH_SIGN_UP_PATH_CONST;
        String requestPath = "/" + containerRequest.getPath();

        logger.info("request path : " + requestPath);

        // No authentication required for login/signup requests
        if (requestPath.equals(nativeLoginPath) ||
                requestPath.equals(nativeSignupPath)) {
            return containerRequest;
        }

        String authHeaderValue = containerRequest.getHeaderValue(AUTH_HEADER);

        if (Strings.isNullOrEmpty(authHeaderValue)) {
            throw new UnauthorizedException("Authentication token not provided");
        }

        logger.info("header value : " + authHeaderValue);

        try {
            // Validate the JWT token and obtain JWT claims
            final FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(authHeaderValue);
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
            throw new UnauthorizedException("Invalid token");
        } catch (SignatureException e) {
            throw new UnauthorizedException("Invalid token signature");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Add logging
            throw new IllegalStateException("Unable to authenticate the request");
        }
    }
}

