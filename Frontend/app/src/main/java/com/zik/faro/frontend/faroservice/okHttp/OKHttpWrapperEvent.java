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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OKHttpWrapperEvent extends BaseFaroOKHttpWrapper implements EventHandler {

    public OKHttpWrapperEvent(final URL baseUrl){
        super(baseUrl, "event");
    }

    @Override
    public void getEvents(final BaseFaroRequestCallback<List<Event>> callback) {
        /*
        * This is dummy data. Replace with actual OKHttp invocations once the 
        * */
        List<Event> eventList = new ArrayList<>();
        int eventCount = 12;
        for(int i=0; i< eventCount; i++){
            DateOffset startDate = new DateOffset(new Date(), 0);
            DateOffset endDate = new DateOffset(new Date(), 3600 * i);
            Location location = new Location("location_"+UUID.randomUUID().toString());
            Event newEvent = new Event("event_"+UUID.randomUUID().toString(), startDate, endDate, false, null, location);
            eventList.add(newEvent);
            callback.onResponse(eventList, null);
        }
    }

    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId){
        DateOffset startDate = new DateOffset(new Date(), 0);
        DateOffset endDate = new DateOffset(new Date(), 3600);
        Location location = new Location("location_"+UUID.randomUUID().toString());
        Event newEvent = new Event(eventId, startDate, endDate, false, null, location);
        callback.onResponse(newEvent, null);
    }
    
    // TODO: Temp use this actual implementation instead of event
    public void getEvent2(final BaseFaroRequestCallback<Event> callback, final String eventId){
        String token = TokenCache.getTokenCache().getAuthToken();
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + eventId)
                .addHeader("Authentication", token)
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }

    public void createEvent(final BaseFaroRequestCallback<MinEvent> callback, EventCreateData eventCreateData){
        // TODO: Validate the eventCreateData
        String token = TokenCache.getTokenCache().getAuthToken();
        String eventPostBody = mapper.toJson(eventCreateData);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create")
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPostBody))
                .addHeader("Authentication", token)
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<MinEvent>(callback, MinEvent.class));
    }
}
