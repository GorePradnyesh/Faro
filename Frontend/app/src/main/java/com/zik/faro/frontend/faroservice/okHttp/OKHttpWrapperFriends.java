package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.FriendsHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OKHttpWrapperFriends extends BaseFaroOKHttpWrapper implements FriendsHandler{
    public OKHttpWrapperFriends(URL baseUrl) {
        super(baseUrl, "friends");
    }

    @Override
    public void inviteFriend(BaseFaroRequestCallback<String> callback, String userId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + "invite/")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), userId))
                    .addHeader(authHeaderName, token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, String.class));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getFriends(BaseFaroRequestCallback<List<MinUser>> callback) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString())
                    .addHeader(authHeaderName, token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            Type minUserList = new TypeToken<ArrayList<MinUser>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, minUserList));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void unFriend(BaseFaroRequestCallback<String> callback, String userId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + "remove?userId=" + userId)
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), userId))
                    .addHeader(authHeaderName, token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, String.class));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }
}
