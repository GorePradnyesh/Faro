package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.frontend.data.Assignment;
import com.zik.faro.frontend.data.AssignmentCount;
import com.zik.faro.frontend.data.Item;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.AssignmentHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class OkHttpWrapperAssignment extends BaseFaroOKHttpWrapper implements AssignmentHandler {
    
    final URL getEventsUrl;
    public OkHttpWrapperAssignment (final URL baseUrl){
        super(baseUrl, "event");
        this.getEventsUrl = super.constructUrl(baseUrl, "events");
    }

    @Override
    public void getAssignments(BaseFaroRequestCallback<Map<String, Assignment>> callback, String eventId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignments") // getting events has a different base url
                    .addHeader(authHeaderName, token)
                    .build();
            // Use GSON to get the TypeToken
            Type assignmentList = new TypeToken<Map<String, Assignment>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Map<String, Assignment>>(callback, assignmentList));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getAssignment(BaseFaroRequestCallback<Assignment> callback, String eventId, String assignmentId, String activityId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        String queryString = "";
        if(token != null) {
            if(activityId != null)
            {
                queryString = "?activityId=" + activityId;
            }
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignment/" + assignmentId + queryString) // getting events has a different base url
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Assignment>(callback, Assignment.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getPendingAssignmentCount(BaseFaroRequestCallback<AssignmentCount> callback, String eventId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignment/pending/count") // getting events has a different base url
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<AssignmentCount>(callback, AssignmentCount.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void deleteAssignment(BaseFaroRequestCallback<String> callback, String eventId, String assignmentId, String activityId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        String queryString = "";
        if(token != null) {
            if(activityId != null)
            {
                queryString = "?activityId=" + activityId;
            }
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignment/" + assignmentId + queryString) // getting events has a different base url
                    .delete()
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void updateAssignment(BaseFaroRequestCallback<String> callback, String eventId, String assignmentId, String activityId, List<Item> items) {
        String token = TokenCache.getTokenCache().getAuthToken();
        String queryString = "";
        String itemList = mapper.toJson(items);
        if(token != null) {
            if(activityId != null)
            {
                queryString = "?activityId=" + activityId;
            }
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignment/" + assignmentId + "/updateItems" + queryString) // getting events has a different base url
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), itemList))
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }        
    }

    @Override
    public void addAssignment(BaseFaroRequestCallback<String> callback, String eventId, Assignment assignment, String activityId) {
        String token = TokenCache.getTokenCache().getAuthToken();
        String queryString = "";
        String assignmentString = mapper.toJson(assignment);
        if(token != null) {
            if(activityId != null)
            {
                queryString = "?activityId=" + activityId;
            }
            Request request = new Request.Builder()
                    .url(this.getEventsUrl + eventId + "/assignment/create" + queryString) // getting events has a different base url
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), assignmentString))
                    .addHeader(authHeaderName, token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, String.class));
        }
        else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }


}
