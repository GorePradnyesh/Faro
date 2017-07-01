package com.zik.faro.frontend.faroservice.spec;

import com.squareup.okhttp.Response;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

/**
 * Created by granganathan on 1/31/16.
 */
public interface LoginHandler {
    void login(final BaseFaroRequestCallback<String> callback, final String email, final String password);
    void login(final BaseFaroRequestCallback<String> callback, final String email, final String password, boolean addToCache);

    OkHttpResponse<String> login(final String email, final String password, boolean addToCache) throws IOException;
}
