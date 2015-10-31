package com.zik.faro.data.user;

/**
 * Created by pgore on 10/31/15.
 */
public class Signup {
    private FaroUser faroUser;
    private String password;

    public Signup(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
    }

    private Signup() {}

    public FaroUser getFaroUser() {
        return faroUser;
    }

    public String getPassword() {
        return password;
    }
}