package com.zik.faro.frontend.faroservice.okHttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.ProfileHandler;

import java.io.IOException;
import java.net.URL;

public class OkHttpWrapperProfile extends BaseFaroOKHttpWrapper implements ProfileHandler {
    public OkHttpWrapperProfile(URL baseUrl) {
        super(baseUrl, "profile");
    }

    @Override
    public void createProfile(final BaseFaroRequestCallback<String> callback, final FaroUser faroUser) {
        // TODO: add param checks
        // TODO: Move create to string constants
        String token = TokenCache.getTokenCache().getToken();
        String eventPutBody = mapper.toJson(faroUser);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create")
                .put(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPutBody))
                .addHeader(authHeaderName, token)
                .build();

        httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
    }

    @Override
    public void getProfile(final BaseFaroRequestCallback<FaroUser> callback, String userId) {
        // TODO: add param checks
        String token = TokenCache.getTokenCache().getToken();
         Request request = new Request.Builder()
                 .url(baseHandlerURL.toString())
                 .addHeader(authHeaderName, token)
                 .build();

        httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<FaroUser>(callback, FaroUser.class));
    }

    @Override
    public void upsertProfile(BaseFaroRequestCallback<FaroUser> callback, FaroUser faroUser) {
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "upsert")
                .put(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), mapper.toJson(faroUser) ))
                .addHeader(authHeaderName, TokenCache.getTokenCache().getToken())
                .build();

        httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<FaroUser>(callback, FaroUser.class));
    }


    private Request createAddTokenRequest(String registrationToken) {
        return new Request.Builder()
                .url(baseHandlerURL.toString() + "add/registrationToken")
                .put(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), registrationToken))
                .addHeader(authHeaderName, TokenCache.getTokenCache().getToken())
                .build();
    }

    private Request createRemoveTokenRequest(String registrationToken) {
        return new Request.Builder()
                .url(baseHandlerURL.toString() + "remove/registrationToken")
                .put(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), registrationToken))
                .addHeader(authHeaderName, TokenCache.getTokenCache().getToken())
                .build();
    }

    @Override
    public void addRegistrationToken(BaseFaroRequestCallback<String> callback, String registrationToken) {
        httpClient.newCall(createAddTokenRequest(registrationToken))
                .enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
    }

    @Override
    public OkHttpResponse<Void> addRegistrationToken(String registrationToken) throws IOException {
        Response response = httpClient.newCall(createAddTokenRequest(registrationToken)).execute();

        if (response.isSuccessful()) {
            return new OkHttpResponse<Void>(null, null);
        }

        return new OkHttpResponse<Void>(new HttpError(response.code(), response.message()));
    }

    @Override
    public void removeRegistrationToken(BaseFaroRequestCallback<String> callback, String registrationToken) {
        httpClient.newCall(createRemoveTokenRequest(registrationToken))
                .enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
    }

    @Override
    public OkHttpResponse<Void> removeRegistrationToken(String registrationToken) throws IOException {
        Response response = httpClient.newCall(createRemoveTokenRequest(registrationToken)).execute();

        if (response.isSuccessful()) {
            return new OkHttpResponse<Void>(null, null);
        }

        return new OkHttpResponse<Void>(new HttpError(response.code(), response.message()));
    }
}
