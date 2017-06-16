package com.zik.faro.frontend.faroservice.auth;


public class FaroUserContext {
    private String email;
    private String firebaseToken;
    private static FaroUserContext faroUserContext = null;

    public static FaroUserContext getInstance(){
        if (faroUserContext != null) {
            return faroUserContext;
        }

        synchronized (FaroUserContext.class) {
            if(faroUserContext == null) {
                faroUserContext = new FaroUserContext();
            }
            return faroUserContext;
        }
    }

    private FaroUserContext(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public FaroUserContext(String email){
        this.email = email;
        //FaroCache faroCache = FaroCache.getOrCreateFaroUserContextCache();
    }
}

