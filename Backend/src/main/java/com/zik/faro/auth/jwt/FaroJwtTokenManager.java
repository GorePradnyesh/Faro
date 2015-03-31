package com.zik.faro.auth.jwt;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        JWTVerifier verifier = new JWTVerifier(JWT_SIGNATURE_SECRET, null, FARO_JWT_ISSUER_VALUE);
        try {
            Map<String, Object> claimsMap = verifier.verify(token);
            return getClaims(claimsMap);
        } catch (IOException e) {
            logger.warn("JWT token is invalid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("JWT token claims are not valid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        } catch (JWTVerifyException e) {
            logger.warn("Invalid claims in the JWT token.", e);
            throw new JwtTokenValidationException(e.getMessage());
        }
    }

    /**
     * Create the JWT token using the claims passed in
     * @param jwtClaims
     * @return JWT token string
     */
    public static String createToken(FaroJwtClaims jwtClaims) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimConstants.ISSUER_KEY, jwtClaims.getIssuer());
        claimsMap.put(JwtClaimConstants.USERNAME, jwtClaims.getUsername());
        claimsMap.put(JwtClaimConstants.EMAIL, jwtClaims.getEmail());
        claimsMap.put(JwtClaimConstants.ISSUED_AT_KEY, jwtClaims.getIssuedAtInMilliSecs());
        claimsMap.put(JwtClaimConstants.EXPIRATION_KEY, jwtClaims.getExpirationInSecs());

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
                .setEmail(claimsMap.get(JwtClaimConstants.EMAIL).toString());

        return faroJwtClaims;
    }

}
