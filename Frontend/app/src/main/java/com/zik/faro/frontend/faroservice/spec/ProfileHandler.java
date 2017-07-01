package com.zik.faro.frontend.faroservice.spec;

import com.squareup.okhttp.Response;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;

public interface ProfileHandler {
    void createProfile(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser);
    void getProfile(final BaseFaroRequestCallback<FaroUser> callback, final String userId);
    void upsertProfile(final BaseFaroRequestCallback<FaroUser> callback, final FaroUser faroUser);
    void addRegistrationToken(final BaseFaroRequestCallback<String> callback, final String registrationToken);
    void removeRegistrationToken(final BaseFaroRequestCallback<String> callback, final String registrationToken);

    OkHttpResponse<Void> addRegistrationToken(final String registrationToken) throws IOException;
    OkHttpResponse<Void> removeRegistrationToken(final String registrationToken) throws IOException;
}
