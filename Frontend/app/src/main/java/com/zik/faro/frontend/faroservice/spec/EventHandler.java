package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.MinEvent;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

public interface EventHandler {
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId);
    public void createEvent(final BaseFaroRequestCallback<MinEvent> callback, EventCreateData eventCreateData);
}
