package com.zik.faro.frontend.faroservice.okHttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.SignupDetails;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.SignupHandler;

import java.io.IOException;
import java.net.URL;

public class OKHttpWrapperSignup extends BaseFaroOKHttpWrapper implements SignupHandler {

    public OKHttpWrapperSignup(final URL baseUrl){
        super(baseUrl, "nativeLogin/signup");
    }

    @Override
    public void signup(BaseFaroRequestCallback<String> callback, final FaroUser faroUser, final String password) {
        SignupDetails signupDetails = new SignupDetails(faroUser, password);
        final String eventPostBody = mapper.toJson(signupDetails);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString())
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPostBody))
                .build();

        SignupHandlerCallback signupHandlerCallback = new SignupHandlerCallback(callback);
        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(signupHandlerCallback, String.class));
    }

    private class SignupHandlerCallback implements BaseFaroRequestCallback<String>{
        BaseFaroRequestCallback<String> callback;

        SignupHandlerCallback(BaseFaroRequestCallback<String> callback){
            this.callback = callback;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            callback.onFailure(request, e);
        }

        @Override
        public void onResponse(String token, HttpError error) {
            if(error == null) {
                TokenCache.setTokenCache(token);
            }
            callback.onResponse(token, error);
        }

    }

}
