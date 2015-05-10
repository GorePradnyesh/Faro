package com.zik.faro.frontend.faroservice.okHttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.frontend.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.spec.ProfileHandler;

import java.net.URL;

public class OkHttpWrapperProfile extends BaseFaroOKHttpWrapper implements ProfileHandler{
    public OkHttpWrapperProfile(URL baseUrl) {
        super(baseUrl, "profile");
    }

    @Override
    public void createProfile(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser) {
        // TODO: add param checks
        // TODO: Move create to string constants
        String eventPutBody = mapper.toJson(faroUser);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create")
                .put(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPutBody))
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
    }

    @Override
    public void getProfile(final BaseFaroRequestCallback<FaroUser> callback, String userId) {
        // TODO: add param checks
         Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + userId)
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<FaroUser>(callback, FaroUser.class));
    }
}
