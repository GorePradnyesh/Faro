package com.zik.faro.auth.jwt;

import com.auth0.jwt.FaroJwtVerifier;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.google.api.client.util.ArrayMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.persistence.datastore.data.user.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by granganathan on 2/6/15.
 */

/**
 * Class used for creating, validating jwt tokens and obtaining claims
 * from jwt tokens for native login/signup
 */
public class FaroJwtTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(FaroJwtTokenManager.class);
    // TODO: Use a different meaningful secret
    private static final String JWT_SIGNATURE_SECRET = "SQAAGREEnsYCx8LXBXyBn9zfzHYZxa0TC4CmJRyZ";
    private static final String FARO_JWT_ISSUER_VALUE = "faro";
    private static final long DEFAULT_EXPIRATION_TIME_SECS = TimeUnit.DAYS.toSeconds(60);

    /**
     * Validates the token string by verifying that its a valid
     * JWT token and verifies the signature of the token
     *
     * @param token
     * @return FaroJwtClaims
     * @throws JwtTokenValidationException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static FaroJwtClaims validateToken(String token) throws JwtTokenValidationException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        JWTVerifier verifier = new FaroJwtVerifier(JWT_SIGNATURE_SECRET, null, FARO_JWT_ISSUER_VALUE);
        try {
            Map<String, Object> claimsMap = verifier.verify(token);
            return getClaims(claimsMap);
        } catch (IOException e) {
            logger.error("JWT token is invalid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("JWT token claims are not valid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        } catch (JWTVerifyException e) {
            logger.error("Invalid claims in the JWT token.", e);
            throw new JwtTokenValidationException(e.getMessage());
        }
    }

    /**
     *
     * @param token
     * @return FaroJwtClaims
     */
    public static FaroJwtClaims obtainClaimsWithNoChecks(String token) throws JwtTokenValidationException {
        FaroJwtVerifier verifier = new FaroJwtVerifier(JWT_SIGNATURE_SECRET, null, FARO_JWT_ISSUER_VALUE);
        try {
            Map<String, Object> claimsMap = verifier.obtainJwtClaimsWithoutValidations(token);
            return getClaims(claimsMap);
        } catch (IOException e) {
            logger.error("JWT token is invalid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        }
    }

    public static Map<String, Object> obtainClaimsMapWithNoChecks(String token) throws JwtTokenValidationException {
        FaroJwtVerifier verifier = new FaroJwtVerifier(JWT_SIGNATURE_SECRET, null, FARO_JWT_ISSUER_VALUE);
        try {
            return verifier.obtainJwtClaimsWithoutValidations(token);
        } catch (IOException e) {
            logger.error("JWT token is invalid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        }
    }


    /**
     * Create the JWT token using the claims passed in
     * @param jwtClaims
     * @return JWT token string
     */
    public static String createToken(FaroJwtClaims jwtClaims) {
        // TODO : Make sure expiration and issuedat are valid values
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimConstants.ISSUER_KEY, jwtClaims.getIssuer());
        claimsMap.put(JwtClaimConstants.USERNAME, jwtClaims.getUsername());
        claimsMap.put(JwtClaimConstants.EMAIL, jwtClaims.getEmail());
        claimsMap.put(JwtClaimConstants.ISSUED_AT_KEY, jwtClaims.getIssuedAtInMilliSecs());
        claimsMap.put(JwtClaimConstants.EXPIRATION_KEY, jwtClaims.getExpirationInSecs());
        claimsMap.put(JwtClaimConstants.JWT_KEY, jwtClaims.getJwtId());

        logger.info("claimsMap: " + claimsMap);

        JWTSigner signer = new JWTSigner(JWT_SIGNATURE_SECRET);

        return  signer.sign(claimsMap);
    }

    /**
     * Create the JWT token with the username  specified
     * Use default values for other claims
     * @param username
     * @return JWT token string
     */
    public static String createToken(String username) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimConstants.ISSUER_KEY, FARO_JWT_ISSUER_VALUE);
        claimsMap.put(JwtClaimConstants.USERNAME, username);
        claimsMap.put(JwtClaimConstants.EMAIL, username);

        JWTSigner signer = new JWTSigner(JWT_SIGNATURE_SECRET);

        return signer.sign(claimsMap, new JWTSigner.Options().setIssuedAt(true)
            .setExpirySeconds(new Integer(new Long(DEFAULT_EXPIRATION_TIME_SECS).intValue())));
    }

    /**
     * Get the Claims object from the claims map
     * returned by the JWT verifier
     * @param claimsMap
     * @return FaroJwtClaims
     */
    private static FaroJwtClaims getClaims(Map<String, Object> claimsMap) {
        FaroJwtClaims faroJwtClaims = new FaroJwtClaims()
                .setIssuer(claimsMap.get(JwtClaimConstants.ISSUER_KEY).toString())
                .setIssuedAtInMilliSecs(Long.parseLong(claimsMap.get(JwtClaimConstants.ISSUED_AT_KEY).toString()))
                .setUsername(claimsMap.get(JwtClaimConstants.USERNAME).toString())
                .setEmail(claimsMap.get(JwtClaimConstants.EMAIL).toString())
                .setExpirationInSecs(Long.parseLong(claimsMap.get(JwtClaimConstants.EXPIRATION_KEY).toString()))
                .setJwtId(claimsMap.get(JwtClaimConstants.JWT_KEY) != null ? claimsMap.get(JwtClaimConstants.JWT_KEY).toString() : null);

        return faroJwtClaims;
    }

    public static FirebaseToken verifyFirebaseToken(String token) {
        Task<FirebaseToken> task = FirebaseAuth.getInstance().verifyIdToken(token);

        try {
            Tasks.await(task, 30, TimeUnit.SECONDS);

            FirebaseToken firebaseToken = task.getResult();
            logger.info(MessageFormat.format("firebaseToken uid = {0}, email = {1}, issuer = {2}, claims = {3}, name = {4}",
                    firebaseToken.getUid(), firebaseToken.getEmail(), firebaseToken.getIssuer(),
                    firebaseToken.getClaims(), firebaseToken.getName()));

            return firebaseToken;

        } catch (ExecutionException e) {
            if (e.getCause() instanceof FirebaseAuthException) {
                logger.error("Invalid firebase token", e);
                throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, "invalid token");
            } else {
                throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user");
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Failed to verify firebase token ", e);
            throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "error in signing up user");
        }
    }

    /**
     * Get the auth provider from the given firebase token
     * @param firebaseToken
     * @return AuthProvider
     */
    @CheckForNull
    public static AuthProvider getAuthProvider(FirebaseToken firebaseToken) {
        String signInProvider = (String) ((ArrayMap<String, Object>) firebaseToken.getClaims().get("firebase")).get("sign_in_provider");
        if (signInProvider != null) {
            switch (signInProvider) {
                case "facebook.com" :
                    return AuthProvider.FACEBOOK;

                case "google.com" :
                    return  AuthProvider.GOOGLE;

            }
        }

        return null;
    }

    /**
     * Get the authprovider given user id from the given firebase token and AuthProvider
     * @param authProvider
     * @param firebaseToken
     * @return
     */
    @CheckForNull
    public static String getAuthProviderUserId(AuthProvider authProvider, FirebaseToken firebaseToken) {
        ArrayMap<String, Object> firebaseClaims = (ArrayMap<String, Object>) firebaseToken.getClaims().get("firebase");
        ArrayMap<String, Object> identities = (ArrayMap<String, Object>) firebaseClaims.get("identities");

        String userId = null;
        if (AuthProvider.FACEBOOK.equals(authProvider)) {
            List<String> identitiesList = (List<String>) identities.get("facebook.com");
            userId = identitiesList.get(0);
        }

        return userId;
    }
}
