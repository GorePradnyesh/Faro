package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.frontend.data.Activity;
import com.zik.faro.frontend.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.ActivityHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OKHttpWrapperActivity extends BaseFaroOKHttpWrapper implements ActivityHandler{
    public OKHttpWrapperActivity(URL baseUrl) {
        super(baseUrl, "event");
    }

    @Override
    public void getActivities(BaseFaroRequestCallback<List<Activity>> callback, String eventId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/activities")
                    .addHeader(authHeaderName, token)
                    .build();
            Type activityList = new TypeToken<ArrayList<Activity>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<List<Activity>>(callback, activityList));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getActivity(BaseFaroRequestCallback<Activity> callback, String eventId, String activityId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/activity/" + activityId)
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Activity>(callback, Activity.class));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void createActivity(BaseFaroRequestCallback<String> callback, String eventId, Activity activity) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null) {
            String pollPostBody = mapper.toJson(activity);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/activity/create/")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void updateActivity(BaseFaroRequestCallback<String> callback, String eventId, String activityId, Activity.ActivityUpdateData updateData) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null) {
            String pollPostBody = mapper.toJson(updateData);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/activity/" + activityId + "/update")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void deleteActivity(BaseFaroRequestCallback<String> callback, String eventId, String activityId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/activity/" + activityId)
                    .delete()
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }


}
