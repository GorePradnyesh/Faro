package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

public interface SignupHandler {
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password);
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password, boolean addToCache);
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password, final String firebaseIdToken);
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password,
                final String firebaseIdToken, boolean addToCache);

    OkHttpResponse<String> signup(FaroUser faroUser, String password, boolean addToCache) throws IOException;
    OkHttpResponse<String> signup(FaroUser faroUser, String password, String firebaseIdToken, boolean addToCache) throws IOException;
}
