package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface EventHandler {
    public void getEvents(final BaseFaroRequestCallback<List<EventInviteStatusWrapper>> callback);
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId);
    public void createEvent(final BaseFaroRequestCallback<Event> callback, Event event);
}
