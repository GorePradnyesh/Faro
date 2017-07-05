package com.zik.faro;

import com.zik.faro.data.user.FaroUser;

/**
 * Created by gaurav on 6/4/17.
 */
public class TestFaroUser {
    private FaroUser faroUser;
    private String token;

    public TestFaroUser(FaroUser faroUser, String token) {
        this.faroUser = faroUser;
        this.token = token;
    }

    public FaroUser getFaroUser() {
        return faroUser;
    }

    public String getToken() {
        return token;
    }
}
