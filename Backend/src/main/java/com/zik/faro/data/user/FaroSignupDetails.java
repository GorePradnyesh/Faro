package com.zik.faro.data.user;

public class FaroSignupDetails {
    private FaroUser faroUser;
    private String password;
    private String firebaseToken;

    private FaroSignupDetails() {}

    public FaroSignupDetails(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
    }

    public FaroSignupDetails(FaroUser faroUser, String password, String firebaseToken) {
        this(faroUser, password);
        this.firebaseToken = firebaseToken;
    }

    public FaroUser getFaroUser() {
        return faroUser;
    }

    public String getPassword() {
        return password;
    }

    // TODO : Will have to make the setters private and still have jaxb working fine
    public void setFaroUser(FaroUser faroUser) {
        this.faroUser = faroUser;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }
}
