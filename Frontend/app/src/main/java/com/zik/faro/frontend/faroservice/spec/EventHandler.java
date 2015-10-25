package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.MinEvent;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface EventHandler {
    public void getEvents(final BaseFaroRequestCallback<List<Event>> callback);
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId);
    public void createEvent(final BaseFaroRequestCallback<Event> callback, EventCreateData eventCreateData);
}
