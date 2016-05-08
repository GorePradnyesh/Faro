package com.zik.faro.frontend.faroservice.okHttp;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
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

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password) {
        this.login(callback, email, password, true);
    }

    @Override
    public void login(BaseFaroRequestCallback<String> callback, String email, String password, boolean addToCache) {
        HttpUrl httpUrl = HttpUrl.parse(baseHandlerURL.toString())
                .newBuilder()
                .addQueryParameter("username", email)
                .build();

        // Note : No need to convert password to Json as it is not required to do so when
        //       when passing a string to RequestBody.create
        Request request = new Request.Builder()
                .url(httpUrl)
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), password))
                .build();

        LoginHandlerCallback loginHandlerCallback = new LoginHandlerCallback(callback, addToCache);
        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(loginHandlerCallback, String.class));
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
