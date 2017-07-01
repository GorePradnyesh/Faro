package com.zik.faro.frontend.faroservice.okHttp;

import android.util.Log;

import com.google.common.base.Strings;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
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
        login(callback, email, password, true);
    }

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password, boolean addToCache) {
        httpClient.newCall(createLoginRequest(email, password, null)).enqueue(
                new DeserializerHttpResponseHandler<String>(new LoginHandlerCallback(callback, addToCache, email), String.class));
    }

    @Override
    public OkHttpResponse<String> login(String email, String password, boolean addToCache) throws IOException {
        return null;
    }

    @Override
    public OkHttpResponse<String> login(String email, String password, String firebaseIdToken, boolean addToCache) throws IOException {
        Response response = httpClient.newCall(createLoginRequest(email, password, firebaseIdToken)).execute();

        if (response.isSuccessful()
                && response.body() !=null
                && addToCache) {

            String token = response.body().string();
            saveTokenAndUserContext(token, email);

            return new OkHttpResponse<String>(token, null);
        }

        return new OkHttpResponse<String>(null, new HttpError(response.code(), response.message()));
    }

    private Request createLoginRequest(String email, String password, String firebaseIdToken) {
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

        Request request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();

        return request;
    }

    private class LoginHandlerCallback implements BaseFaroRequestCallback<String> {
        private BaseFaroRequestCallback<String> callback;
        private boolean addToCache;
        private String email;

        LoginHandlerCallback(BaseFaroRequestCallback<String> callback, boolean addToCache, String email) {
            this.callback = callback;
            this.addToCache = addToCache;
            this.email = email;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            callback.onFailure(request, e);
        }

        @Override
        public void onResponse(String token, HttpError error) {
            if (error == null && addToCache) {
                saveTokenAndUserContext(token, email);
            }

            callback.onResponse(token, error);
        }

    }

    private void saveTokenAndUserContext(String token, String email) {
        TokenCache.getTokenCache().setToken(token);
        FaroUserContext.getInstance().setEmail(email);
        FaroCache.getFaroUserContextCache().saveFaroCacheToDisk("email", email);
    }
}
