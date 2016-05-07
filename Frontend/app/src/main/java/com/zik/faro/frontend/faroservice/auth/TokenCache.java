package com.zik.faro.frontend.faroservice.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for caching tokens and retrieving/storing tokens to SharedPreferences
 */
public class TokenCache {
    private String TAG = "TokenCache";
    private static TokenCache instance;
    private JWTToken jwtToken;
    private static final String SHARED_PREFS_NAME = "FaroPrefsFile";
    private static SharedPreferences sharedPrefs;

    private TokenCache(Context context) {
        sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenCache getOrCreateTokenCache(Context context) {
        if (instance == null) {
            instance = new TokenCache(context);
        }
        return instance;
    }

    public static synchronized TokenCache getTokenCache() {
        if (instance == null) {
            throw new IllegalStateException("Token cache has not been initialized.");
        }
        return instance;
    }

    /**
     *
     * Call this on a successful login/sign-up
     * @param token
     */
    public synchronized void setToken(final String token) {
        jwtToken = new JWTToken(token);
        saveTokenStringToDisk(token);
    }

    /**
     * Get token string from cache
     * If token is not present in cache, then load token from disk
     * and cache it
     * @return
     */
    public synchronized String getToken() {
        if (jwtToken == null) {
            String tokenStringFromDisk = loadTokenStringFromDisk();
            if (!Strings.isNullOrEmpty(tokenStringFromDisk)) {
                setToken(tokenStringFromDisk);
            } else {
                throw new RuntimeException("No token present");
            }
        } else if(jwtToken.hasExpired()){
            throw new RuntimeException("Token expired or getToken invoked before signing-in");
            //TODO: Add sync and async methods   - ???
        }

        return jwtToken.getTokenString();
    }

    /**
     * Delete token from cache and disk
     * Call this on a successful logout
     */
    public synchronized void deleteToken() {
        // Delete token from sharedprefs
        deleteTokenFromDisk();
        jwtToken = null;
    }


    /**
     * Store token to disk
     *
     * @param token
     */
    private void saveTokenStringToDisk(String token) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("token", token);
        // Commit the edits!
        editor.commit();

        // Test token restore
        String sharedPrefsToken = sharedPrefs.getString("token", "");
        Log.i(TAG, "sharedPrefsToken = " + sharedPrefsToken);
    }
 
    /**
     * Load token from disk
     * @return
     */
    private String loadTokenStringFromDisk() {
        return sharedPrefs.getString("token", "");
    }

    private static void deleteTokenFromDisk() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }

    private static class JWTToken {
        private String tokenString;
        private FaroJwtClaims claims;

        public JWTToken(final String tokenString){
            this.tokenString = tokenString;
            this.claims = getClaimsFromToken(this.tokenString);
        }

        public String getTokenString() {
            return  tokenString;
        }

        public FaroJwtClaims getClaims() {
            return claims;
        }

        public boolean hasExpired(){
            long currentTime = System.currentTimeMillis() / 1000;
            if(currentTime > claims.getExpirationInSecs()){
                return true;
            }
            return false;
        }

        private static FaroJwtClaims getClaimsFromToken(final String token){
            try {
                String[] pieces = token.split("\\.");
                // check number of segments
                if (pieces.length != 3) {
                    throw new IllegalStateException("Wrong number of segments: " + pieces.length);
                }
                String jsonString = new String(Base64.decode(pieces[1], Base64.DEFAULT), "UTF-8");
                Map<String, Object> claimsMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
                Double issuedAt = Double.parseDouble(claimsMap.get(JwtClaimConstants.ISSUED_AT_KEY).toString());
                Double expiresAt = Double.parseDouble(claimsMap.get(JwtClaimConstants.EXPIRATION_KEY).toString());
                FaroJwtClaims faroJwtClaims = new FaroJwtClaims()
                        .setIssuer(claimsMap.get(JwtClaimConstants.ISSUER_KEY).toString())
                        .setIssuedAtInMilliSecs(issuedAt.longValue())
                        .setUsername(claimsMap.get(JwtClaimConstants.USERNAME).toString())
                        .setEmail(claimsMap.get(JwtClaimConstants.EMAIL).toString())
                        .setExpirationInSecs(expiresAt.longValue());
                return faroJwtClaims;
            } catch (UnsupportedEncodingException e) {
                // this condition should never hit. If it does something very bad has happened
                throw new RuntimeException(e);
            }
        }
    }
}
