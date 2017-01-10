package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface EventHandler {
    void getEvents(final BaseFaroRequestCallback<List<EventInviteStatusWrapper>> callback);
    void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId);
    void createEvent(final BaseFaroRequestCallback<Event> callback, Event event);
    void deleteEvent(final BaseFaroRequestCallback<String> callback, final String eventId);
    void getEventInvitees(final BaseFaroRequestCallback<InviteeList> callback, final String eventId);
    void addInviteesToEvent(final BaseFaroRequestCallback<String> callback, final String eventId, final AddFriendRequest addFriendRequest);
    void updateEventUserInviteStatus(final BaseFaroRequestCallback<String> callback, final String eventId, EventInviteStatus inviteStatus);
}
