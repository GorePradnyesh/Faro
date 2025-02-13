package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.PollHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OKHttpWrapperPoll extends BaseFaroOKHttpWrapper implements PollHandler {
    private static final String pollPath = "poll";
    private static final String pollsPath = "polls";
    
    
    public OKHttpWrapperPoll(final URL baseURL){
        super(baseURL, "event");
    }
    
    @Override
    public void getPolls(BaseFaroRequestCallback<List<Poll>> callback, String eventId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/polls/")
                    .addHeader(authHeaderName, token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            Type pollList = new TypeToken<ArrayList<Poll>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<List<Poll>>(callback, pollList));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getPoll(BaseFaroRequestCallback<Poll> callback, String eventId, String pollId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/")
                    .addHeader(authHeaderName, token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Poll>(callback, Poll.class));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void createPoll(BaseFaroRequestCallback<Poll> callback, String eventId, Poll poll) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            String pollPostBody = mapper.toJson(poll);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/create/")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, Poll.class));
        } else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void getUnvotedCount(BaseFaroRequestCallback<Integer> callback, final String eventId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/polls/unvoted/count/")
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Integer>(callback, Integer.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void castVote(BaseFaroRequestCallback<String> callback, String eventId, String pollId, Set<String> options) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            String voteBody = mapper.toJson(options);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/vote/")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), voteBody))
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void closePoll(BaseFaroRequestCallback<String> callback, String eventId, String pollId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/close/")
                    .addHeader("Authentication", token)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void deletePoll(BaseFaroRequestCallback<String> callback, String eventId, String pollId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId)
                    .addHeader("Authentication", token)
                    .delete()
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<String>(callback, String.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void updatePollOptions(BaseFaroRequestCallback<Poll> callback, String eventId, String pollId, UpdateCollectionRequest<Poll, PollOption> updateCollectionRequest) {
        String token = TokenCache.getTokenCache().getToken();
        if (token != null) {
            String pollPostBody = mapper.toJson(updateCollectionRequest);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/updatePollOptions")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, Poll.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void castVote(BaseFaroRequestCallback<Poll> callback, String eventId, String pollId, UpdateCollectionRequest<Poll, String> updateCollectionRequest) {
        String token = TokenCache.getTokenCache().getToken();
        if (token != null) {
            String pollPostBody = mapper.toJson(updateCollectionRequest);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/castVote")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, Poll.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void updatePoll(final BaseFaroRequestCallback<Poll> callback, final String eventId, final String pollId,
                           UpdateRequest<Poll> updateRequest) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            String pollPostBody = mapper.toJson(updateRequest);
            Request request = new Request.Builder()
                    .url(baseHandlerURL.toString() + eventId + "/poll/" + pollId + "/updatePoll")
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), pollPostBody))
                    .addHeader("Authentication", token)
                    .addHeader("Accept",DEFAULT_CONTENT_TYPE)
                    .build();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<>(callback, Poll.class));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    private void update() {

    }
}
