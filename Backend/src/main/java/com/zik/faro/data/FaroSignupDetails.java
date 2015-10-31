package com.zik.faro.data;

import com.zik.faro.data.user.FaroUser;
public class FaroSignupDetails {
    private FaroUser faroUser;
    public FaroSignupDetails(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
    }

    private String password;

    private FaroSignupDetails() {}

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
