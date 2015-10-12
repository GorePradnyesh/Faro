package com.zik.faro.frontend.faroservice.okHttp;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.frontend.data.DateOffset;
import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.Location;
import com.zik.faro.frontend.data.MinEvent;

import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.EventHandler;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OKHttpWrapperEvent extends BaseFaroOKHttpWrapper implements EventHandler {
    
    
    final URL getEventsUrl;
    public OKHttpWrapperEvent(final URL baseUrl){
        super(baseUrl, "event");
        this.getEventsUrl = super.constructUrl(baseUrl, "events");
    }

    @Override
    public void getEvents(final BaseFaroRequestCallback<List<Event>> callback) {
        String token = TokenCache.getTokenCache().getAuthToken();
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

    public void getEvent2(final BaseFaroRequestCallback<Event> callback, final String eventId){
        DateOffset startDate = new DateOffset(new Date(), 0);
        DateOffset endDate = new DateOffset(new Date(), 3600);
        Location location = new Location("location_"+UUID.randomUUID().toString());
        Event newEvent = new Event(eventId, Calendar.getInstance(), Calendar.getInstance(), false, null, location);
        callback.onResponse(newEvent, null);
    }
    
    // TODO: Temp use this actual implementation instead of event
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId){
        String token = TokenCache.getTokenCache().getAuthToken();
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + eventId)
                .addHeader("Authentication", token)
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }

    public void createEvent(final BaseFaroRequestCallback<Event> callback, EventCreateData eventCreateData){
        // TODO: Validate the eventCreateData
        String token = TokenCache.getTokenCache().getAuthToken();
        String eventPostBody = mapper.toJson(eventCreateData);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create")
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPostBody))
                .addHeader("Authentication", token)
                .build();
        
        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }
}
