package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

public interface SignupHandler {
    public void signup(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password);
}
