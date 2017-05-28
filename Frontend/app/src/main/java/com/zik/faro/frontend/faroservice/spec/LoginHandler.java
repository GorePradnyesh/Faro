package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

/**
 * Created by granganathan on 1/31/16.
 */
public interface LoginHandler {
    void login(final BaseFaroRequestCallback<String> callback, final String email, final String password);
    void login(final BaseFaroRequestCallback<String> callback, final String email, final String password, final String firebaseIdToken);
    void login(final BaseFaroRequestCallback<String> callback, final String email, final String password,
               final String firebaseIdToken, boolean addToCache);
}
