package com.zik.faro.frontend.faroservice.okHttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
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
        signup(callback, faroUser, password, true);
    }

    @Override
    public void signup(BaseFaroRequestCallback<String> callback, FaroUser faroUser, String password, boolean addToCache) {
        signup(callback, faroUser, password, null, addToCache);
    }

    @Override
    public void signup(BaseFaroRequestCallback<String> callback, FaroUser faroUser, String password, String firebaseIdToken) {
        signup(callback, faroUser, password, firebaseIdToken, true);
    }

    @Override
    public void signup(BaseFaroRequestCallback<String> callback, FaroUser faroUser,
                       String password, String firebaseIdToken, boolean addToCache) {
        Request request = createSignupRequest(faroUser, password, firebaseIdToken);
        SignupHandlerCallback signupHandlerCallback = new SignupHandlerCallback(callback, addToCache, faroUser.getId());
        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(signupHandlerCallback, String.class));
    }

    @Override
    public OkHttpResponse<String> signup(FaroUser faroUser, String password, boolean addToCache) throws IOException {
        return signup(faroUser, password, null, addToCache);
    }

    @Override
    public OkHttpResponse<String> signup(FaroUser faroUser, String password, String firebaseIdToken, boolean addToCache) throws IOException {
        Request request = createSignupRequest(faroUser, password, firebaseIdToken);

        Response response = this.httpClient.newCall(request).execute();

        if (response.isSuccessful()
                && response.body() != null
                && addToCache ) {

            String token = response.body().string();
            saveTokenAndUserContext(token, faroUser.getId());

            return new OkHttpResponse<>(token, null);
        }

        return new OkHttpResponse<>(null, new HttpError(response.code(), response.message()));
    }

    private Request createSignupRequest(FaroUser faroUser, String password) {
        return createSignupRequest(faroUser, password, null);
    }

    private Request createSignupRequest(FaroUser faroUser, String password, String firebaseToken) {
        return new Request.Builder()
                .url(baseHandlerURL.toString())
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE),
                        mapper.toJson(new FaroSignupDetails(faroUser, password, firebaseToken))))
                .build();
    }

    private class SignupHandlerCallback implements BaseFaroRequestCallback<String>{
        private BaseFaroRequestCallback<String> callback;
        private boolean addToCache;
        private String email;

        SignupHandlerCallback(BaseFaroRequestCallback<String> callback, boolean addToCache, String email){
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
                TokenCache.getTokenCache().setToken(token);
                FaroUserContext.getInstance().setEmail(email);
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
