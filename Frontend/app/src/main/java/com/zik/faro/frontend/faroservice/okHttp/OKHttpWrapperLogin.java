package com.zik.faro.frontend.faroservice.okHttp;

import android.util.Log;

import com.google.common.base.Strings;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.LoginHandler;

import java.io.IOException;
import java.net.URL;

/**
 * Created by granganathan on 1/31/16.
 */
public class OKHttpWrapperLogin extends BaseFaroOKHttpWrapper implements LoginHandler {
    public OKHttpWrapperLogin(final URL baseUrl){
        super(baseUrl, "nativeLogin/login");
    }

    protected OKHttpWrapperLogin(final URL baseUrl, String pathPrefix){
        super(baseUrl, pathPrefix);
    }

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password) {
        login(callback, email, password, null, true);
    }

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password, String firebaseIdToken) {
        login(callback, email, null, firebaseIdToken, true);
    }

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password, final String firebaseIdToken, boolean addToCache) {
        HttpUrl httpUrl = HttpUrl.parse(baseHandlerURL.toString())
                .newBuilder()
                .addQueryParameter("username", email)
                .build();

        // Note : No need to convert password to Json as it is not required to do so when
        //       when passing a string to RequestBody.create
        RequestBody requestBody = null;
        if (!Strings.isNullOrEmpty(password)) {
            requestBody = RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), password);
        } else if (!Strings.isNullOrEmpty(firebaseIdToken)) {
            requestBody = RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), firebaseIdToken);
        }

        if (requestBody != null) {
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(requestBody)
                    .build();

            LoginHandlerCallback loginHandlerCallback = new LoginHandlerCallback(callback, addToCache);
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(loginHandlerCallback, String.class));
        }
    }

    private class LoginHandlerCallback implements BaseFaroRequestCallback<String>{
        BaseFaroRequestCallback<String> callback;
        boolean addToCache;

        LoginHandlerCallback(BaseFaroRequestCallback<String> callback, boolean addToCache){
            this.callback = callback;
            this.addToCache = addToCache;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            callback.onFailure(request, e);
        }

        @Override
        public void onResponse(String token, HttpError error) {
            if(error == null && this.addToCache) {
                TokenCache.getTokenCache().setToken(token);
            }
            callback.onResponse(token, error);
        }

    }
}
