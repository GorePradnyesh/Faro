package com.zik.faro.api.authentication;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
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
public class FaroJwtTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(FaroJwtTokenManager.class);
    private static final String JWT_SIGNATURE_SECRET = "SQAAGREEnsYCx8LXBXyBn9zfzHYZxa0TC4CmJRyZ";
    private static final String FARO_JWT_ISSUER_VALUE = "faro";
    private static final long DEFAULT_EXPIRATION_TIME_SECS = TimeUnit.DAYS.toSeconds(60);

    /**
     * Validates the token string by verifying that its a valid
     * JWT token and verifies the signature of the token
     *
     * @param token
     * @return
     * @throws JwtTokenValidationException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static FaroJwtClaims validateToken(String token) throws JwtTokenValidationException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        JWTVerifier verifier = new JWTVerifier(Base64.encodeBase64String(JWT_SIGNATURE_SECRET.getBytes()),
                null, FARO_JWT_ISSUER_VALUE);
        try {
            Map<String, Object> claimsMap = verifier.verify(token);
            return getClaims(claimsMap);
        } catch (IOException e) {
            logger.error("jwt token is invalid. ", e);
            throw new JwtTokenValidationException(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("jwt token claims are not valid.", e);
            throw new JwtTokenValidationException(e.getMessage());
        }
    }

    /**
     * Create the JWT token using the claims passed in
     * @param jwtClaims
     * @return
     */
    public static String createToken(FaroJwtClaims jwtClaims) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimConstants.ISSUER_KEY, jwtClaims.getIssuer());
        claimsMap.put(JwtClaimConstants.USERNAME, jwtClaims.getUsername());
        claimsMap.put(JwtClaimConstants.EMAIL, jwtClaims.getEmail());
        claimsMap.put(JwtClaimConstants.ISSUED_AT_KEY, jwtClaims.getIssuedAtTimeInMilliSecs());
        claimsMap.put(JwtClaimConstants.EXPIRATION_KEY, jwtClaims.getExpirationTimeInMilliSecs());

        JWTSigner signer = new JWTSigner(JWT_SIGNATURE_SECRET);

        return  signer.sign(claimsMap);
    }

    /**
     * Create the JWT token with the username  specified
     * Use default values for other claims
     * @param username
     * @return
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
     * @return
     */
    private static FaroJwtClaims getClaims(Map<String, Object> claimsMap) {
        FaroJwtClaims faroJwtClaims = new FaroJwtClaims()
                .setIssuer(claimsMap.get(JwtClaimConstants.ISSUER_KEY).toString())
                .setIssuedAt(Long.parseLong(claimsMap.get(JwtClaimConstants.ISSUED_AT_KEY).toString()))
                .setUsername(claimsMap.get(JwtClaimConstants.USERNAME).toString())
                .setEmail(claimsMap.get(JwtClaimConstants.EMAIL).toString());

        return faroJwtClaims;
    }

    /**
     * Class for specifying the claims for creating a token
     * or for retrieving the claim from a token
     */
    public static class FaroJwtClaims implements JwtClaims {
        private String issuer;
        private long issuedAt;
        private long expiration;
        private String username;
        private String email;

        public FaroJwtClaims(String issuer, long issuedAt, String username, String email) {
            this.issuer = issuer;
            this.issuedAt = issuedAt;
            this.username = username;
            this.email = email;
        }

        public FaroJwtClaims() {}

        @Override
        public String getIssuer() {
            return issuer;
        }

        @Override
        public long getIssuedAtTimeInMilliSecs() {
            return issuedAt;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public long getExpirationTimeInMilliSecs() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }

        public FaroJwtClaims setIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public FaroJwtClaims setIssuedAt(long issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public FaroJwtClaims setUsername(String username) {
            this.username = username;
            return this;
        }

        public FaroJwtClaims setEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder()
                    .append("[")

                    .append(JwtClaimConstants.ISSUER_KEY)
                    .append(": ")
                    .append(getIssuer())
                    .append(", ")

                    .append(JwtClaimConstants.ISSUED_AT_KEY)
                    .append(": ")
                    .append(getIssuedAtTimeInMilliSecs())
                    .append(", ")

                    .append(JwtClaimConstants.USERNAME)
                    .append(": ")
                    .append(getUsername())
                    .append(", ")

                    .append(JwtClaimConstants.EMAIL)
                    .append(": ")
                    .append(getEmail())

                    .append("]");

            return builder.toString();
        }
    }
}
