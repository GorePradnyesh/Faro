package com.zik.faro.frontend.faroservice;

import android.app.Activity;

import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 6/24/17.
 */

public class FaroLoginJob extends FaroBaseSignInJob {
    private String password;

    public FaroLoginJob(String email, String password) {
        super(email);
        this.password = password;
    }

    @Override
    public OkHttpResponse<String> signIn() throws IOException {
        // Login to faro app server
        return faroServiceHandler.getLoginHandler().login(email, password, true);
    }


}
