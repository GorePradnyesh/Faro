package com.zik.faro.frontend.faroservice;

import android.app.Activity;

import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 6/25/17.
 */

public class FaroSignupJob extends FaroBaseSignInJob {
    private FaroUser newFaroUser;

    public FaroSignupJob(FaroUser newFaroUser, String password) {
        super(newFaroUser.getId(), password);
        this.newFaroUser = newFaroUser;
    }

    @Override
    public OkHttpResponse<String> signIn() throws IOException {
        return faroServiceHandler.getSignupHandler().signup(newFaroUser, password, true);
    }
}
