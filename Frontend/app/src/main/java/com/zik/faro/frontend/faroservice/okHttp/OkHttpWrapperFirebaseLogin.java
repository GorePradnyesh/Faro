package com.zik.faro.frontend.faroservice.okHttp;

import java.net.URL;

/**
 * Created by gaurav on 5/27/17.
 */

public class OkHttpWrapperFirebaseLogin extends OKHttpWrapperLogin {

    public OkHttpWrapperFirebaseLogin(URL baseUrl) {
        super(baseUrl, "firebaseLogin/login");
    }
}
