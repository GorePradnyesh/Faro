package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

public interface ProfileHandler {
    public void createProfile(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser);
    public void getProfile(final BaseFaroRequestCallback<FaroUser> callback, final String userId);
}
