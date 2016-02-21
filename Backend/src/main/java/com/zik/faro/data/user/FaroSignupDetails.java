package com.zik.faro.data.user;

public class FaroSignupDetails {
    private FaroUser faroUser;
    private String password;

    private FaroSignupDetails() {}

    public FaroSignupDetails(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
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
}
