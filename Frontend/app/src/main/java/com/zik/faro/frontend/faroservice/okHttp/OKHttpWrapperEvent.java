package com.zik.faro.frontend.faroservice.okHttp;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.Event;

import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.EventHandler;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OKHttpWrapperEvent extends BaseFaroOKHttpWrapper implements EventHandler {
    
    
    final URL getEventsUrl;
    public OKHttpWrapperEvent(final URL baseUrl){
        super(baseUrl, "event");
        this.getEventsUrl = super.constructUrl(baseUrl, "events");
    }

    @Override
    public void getEvents(final BaseFaroRequestCallback<List<Event>> callback) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.getEventsUrl) // getting events has a different base url
                    .addHeader(authHeaderName, token)
                    .build();
            // Use GSON to get the TypeToken
            Type eventList = new TypeToken<ArrayList<Event>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<List<Event>>(callback, eventList));
        }else{
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }


    // TODO: Temp use this actual implementation instead of event
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId){
        String token = TokenCache.getTokenCache().getToken();
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + eventId + "/details/")
                .addHeader("Authentication", token)
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }

    public void createEvent(final BaseFaroRequestCallback<Event> callback, Event event){
        // TODO: Validate the eventCreateData
        String token = TokenCache.getTokenCache().getToken();
        String eventPostBody = mapper.toJson(event);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create")
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPostBody))
                .addHeader("Authentication", token)
                .build();
        
        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }
}
