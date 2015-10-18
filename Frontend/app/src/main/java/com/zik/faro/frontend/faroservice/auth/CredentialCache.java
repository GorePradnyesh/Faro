package com.zik.faro.frontend.faroservice.auth;

/**
 * Cache which holds onto the user credentials until signout
 */
public class CredentialCache {
    private String username;
    private String email;

    private static CredentialCache sCredentialCache;

    private CredentialCache(){}

    private CredentialCache(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // == Singleton
    public static CredentialCache getCredentialCache(){
        return sCredentialCache;
    }

    /**
     * Should be invoked right after a successful sign-in
     * @param username
     * @param password
     */
    public static void initCredentialCache(final String username, final String password){
        synchronized (CredentialCache.class){
            sCredentialCache = new CredentialCache(username, password);
        }
    }

    /**
     * Should be invoked after a success sign-out
     */
    public static void resetCredentialCache(){
        synchronized (CredentialCache.class){
            sCredentialCache = null;
        }
    }

}
