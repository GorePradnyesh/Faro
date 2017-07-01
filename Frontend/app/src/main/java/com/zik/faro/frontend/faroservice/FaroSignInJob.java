package com.zik.faro.frontend.faroservice;

import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by gaurav on 6/24/17.
 */

public interface FaroSignInJob extends Runnable {
    OkHttpResponse<String> signIn() throws IOException;
}
