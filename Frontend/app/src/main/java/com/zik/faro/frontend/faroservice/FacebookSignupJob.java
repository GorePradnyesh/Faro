package com.zik.faro.frontend.faroservice;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

import static com.zik.faro.frontend.EventListHandler.serviceHandler;

/**
 * Created by gaurav on 7/1/17.
 */

public class FacebookSignupJob extends FacebookLoginJob {
    public FacebookSignupJob(AccessToken facebookAccessToken) {
        super(facebookAccessToken);
    }

    @Override
    protected OkHttpResponse<String> faroSignIn(FirebaseUser firebaseUser, String firebaseAuthToken) throws IOException {
        //return serviceHandler.getFirebaseLoginHandler().login(firebaseUser.getEmail(), null, firebaseAuthToken, true);
        FaroUser newFaroUser = new FaroUser(firebaseUser.getEmail(), firebaseUser.getDisplayName(), null,
                null, null, null, null);

        return serviceHandler.getSignupHandler().signup(newFaroUser, null, firebaseAuthToken, true);
    }
}
