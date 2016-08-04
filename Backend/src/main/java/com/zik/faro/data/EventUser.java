package com.zik.faro.data;

import com.zik.faro.data.user.EventInviteStatus;

public class EventUser {
    private String id;
    private String eventRef;
    private String faroUserRef;
    private EventInviteStatus inviteStatus;
    private String ownerId;

    public EventUser(){    // To satisfy JAXB

    }


    public EventUser(final String eventId, final String faroUserId){
        this.id = generateEventUserId(eventId, faroUserId);
        this.eventRef = eventId;
        this.faroUserRef = faroUserId;
        this.inviteStatus = EventInviteStatus.INVITED;
    }

    public EventUser(final String eventId, final String faroUserId, final String ownerId){
        this(eventId,faroUserId);
        this.ownerId = ownerId;
    }

    private String generateEventUserId(final String eventId, final String faroUserId){
        // NOTE: Since eventId and faroUserId are unique by themselves, the EventUserId will also be unique.
        // this ensures that repeated calls with the same argument are idempotent.
        return eventId + "/" + faroUserId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent(){
        return this.eventRef;
    }

    public String getFaroUser(){
        return this.faroUserRef;
    }

    public void setInviteStatus(EventInviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }
    public EventInviteStatus getInviteStatus(){
        return this.inviteStatus;
    }

    public void setAccepted(){
        this.inviteStatus = EventInviteStatus.ACCEPTED;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
