package com.zik.faro.frontend.faroservice.auth.facebook;

import com.google.firebase.auth.FirebaseUser;
import com.zik.faro.frontend.faroservice.auth.FaroSignInJob;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 7/2/17.
 */

public interface FacebookSignInJob extends FaroSignInJob {
    OkHttpResponse<String> faroSignIn(FirebaseUser firebaseUser, String firebaseAuthToken) throws IOException;
}
