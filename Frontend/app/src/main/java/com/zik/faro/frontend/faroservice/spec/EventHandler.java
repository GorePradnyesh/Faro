package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface EventHandler {
    public void getEvents(final BaseFaroRequestCallback<List<EventInviteStatusWrapper>> callback);
    public void getEvent(final BaseFaroRequestCallback<Event> callback, final String eventId);
    public void createEvent(final BaseFaroRequestCallback<Event> callback, Event event);
    public void deleteEvent(final BaseFaroRequestCallback<String> callback, final String eventId);
    public void getEventInvitees(final BaseFaroRequestCallback<InviteeList> callback, final String eventId);
    public void addInviteesToEvent(final BaseFaroRequestCallback<String> callback, final String eventId, final AddFriendRequest addFriendRequest);
    public void updateEventUserInviteStatus(final BaseFaroRequestCallback<String> callback, final String eventId, EventInviteStatus inviteStatus);
    public void updateEvent(final BaseFaroRequestCallback<Event> callback, String eventId, Event event);
}
