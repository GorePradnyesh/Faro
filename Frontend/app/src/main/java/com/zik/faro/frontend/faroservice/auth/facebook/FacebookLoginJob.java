package com.zik.faro.frontend.faroservice.auth.facebook;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseUser;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 7/2/17.
 */

public class FacebookLoginJob extends FacebookBaseSignInJob {
    public FacebookLoginJob(AccessToken facebookAccessToken) {
        super(facebookAccessToken);
    }

    @Override
    public OkHttpResponse<String> faroSignIn(FirebaseUser firebaseUser, String firebaseAuthToken) throws IOException {
        return faroServiceHandler.getFirebaseLoginHandler().login(firebaseUser.getEmail(), null, firebaseAuthToken, true);
    }

}
