package com.zik.faro.frontend.faroservice.auth.facebook;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 7/1/17.
 */

public class FacebookSignupJob extends FacebookBaseSignInJob implements FacebookSignInJob {
    public FacebookSignupJob(AccessToken facebookAccessToken) {
        super(facebookAccessToken);
    }

    @Override
    public OkHttpResponse<String> faroSignIn(FirebaseUser firebaseUser, String firebaseAuthToken) throws IOException {
        FaroUser newFaroUser = new FaroUser(firebaseUser.getEmail(), firebaseUser.getDisplayName(), null,
                null, null, null, null);

        return faroServiceHandler.getSignupHandler().signup(newFaroUser, null, firebaseAuthToken, true);
    }
}
