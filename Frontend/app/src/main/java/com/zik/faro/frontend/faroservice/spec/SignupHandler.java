package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

public interface SignupHandler {
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password);
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password, final String firebaseIdToken);
    void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password,
                final String firebaseIdToken, boolean addToCache);
}
