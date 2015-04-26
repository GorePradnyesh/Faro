package com.zik.faro.frontend.faroservice.okHttp;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.MinEvent;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.spec.EventHandler;

import java.net.URL;

public class OKHttpWrapperEvent extends BaseFaroOKHttpWrapper implements EventHandler {

    public OKHttpWrapperEvent(final URL baseUrl){
        super(baseUrl, "event");
    }

    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId){
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + eventId + "?Signature=dummySignature")
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<Event>(callback, Event.class));
    }

    public void createEvent(final BaseFaroRequestCallback<MinEvent> callback, EventCreateData eventCreateData){
        // TODO: Validate the eventCreateData
        // TODO: Add authentication logic
        String eventPostBody = mapper.toJson(eventCreateData);
        Request request = new Request.Builder()
                .url(baseHandlerURL.toString() + "create" + "?Signature=dummySignature")
                .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), eventPostBody))
                .build();

        this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<MinEvent>(callback, MinEvent.class));
    }
}
