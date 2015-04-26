package com.zik.faro.frontend.faroservice.auth;

import android.util.Base64;

import com.auth0.jwt.JWTSigner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TokenCache {
    // TODO: temporarily hold onto the secret
    // If the secret changes on the server this will break the functional tests
    private static final String JWT_SIGNATURE_SECRET = "SQAAGREEnsYCx8LXBXyBn9zfzHYZxa0TC4CmJRyZ";
    private JWTToken token;

    private static TokenCache sTokenCache;

    // Singleton
    public static synchronized TokenCache getsTokenCache(){
            return sTokenCache;
    }

    /**
     * Call this on a successful sign-out
     */
    public static synchronized  void flushAuthCache(){
       sTokenCache = null;
    }

    /**
     * Call this on a successful sign-in/sign-up
     * @param token
     */
    public static synchronized void setsTokenCache(final String token){
        sTokenCache = new TokenCache(token);
    }


    public String getAuthToken(){
        if(this.token != null || this.token.hasExpired()){
            CredentialCache credentialCache = CredentialCache.getCredentialCache();
            if(credentialCache == null){
                throw new RuntimeException("getAuthToken invoked before signing-in");
            }
            //TODO: Add sync and async methods
        }
        return this.token.tokenString;
    }

    // =========

    private TokenCache(String token){
        this.token = new JWTToken(token);
    };

    private static class JWTToken{
        private String tokenString;
        FaroJwtClaims claims;

        public JWTToken(final String tokenString){
            this.tokenString = tokenString;
            this.claims = getClaimsFromToken(this.tokenString);
        }

        public boolean hasExpired(){
            long currentTime = System.currentTimeMillis() / 1000;
            if(currentTime > claims.getExpirationInSecs()){
                return true;
            }
            return false;
        }
    }


   private static FaroJwtClaims getClaimsFromToken(final String token){
       try {
           String jsonString = new String(Base64.decode(token, Base64.DEFAULT), "UTF-8");
           Map<String, Object> claimsMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
           FaroJwtClaims faroJwtClaims = new FaroJwtClaims()
                   .setIssuer(claimsMap.get(JwtClaimConstants.ISSUER_KEY).toString())
                   .setIssuedAtInMilliSecs(Long.parseLong(claimsMap.get(JwtClaimConstants.ISSUED_AT_KEY).toString()))
                   .setUsername(claimsMap.get(JwtClaimConstants.USERNAME).toString())
                   .setEmail(claimsMap.get(JwtClaimConstants.EMAIL).toString());
           return faroJwtClaims;
       } catch (UnsupportedEncodingException e) {
           // this condition should never hit. If it does something very bad has happened
           throw new RuntimeException(e);
       }
   }

    // TODO: temporary method
    public static String createToken(FaroJwtClaims jwtClaims) {
        // TODO : Make sure expiration and issuedat are valid values
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(JwtClaimConstants.ISSUER_KEY, jwtClaims.getIssuer());
        claimsMap.put(JwtClaimConstants.USERNAME, jwtClaims.getUsername());
        claimsMap.put(JwtClaimConstants.EMAIL, jwtClaims.getEmail());
        claimsMap.put(JwtClaimConstants.ISSUED_AT_KEY, jwtClaims.getIssuedAtInMilliSecs());
        claimsMap.put(JwtClaimConstants.EXPIRATION_KEY, jwtClaims.getExpirationInSecs());

        JWTSigner signer = new JWTSigner(JWT_SIGNATURE_SECRET);

        return  signer.sign(claimsMap);
    }
}
