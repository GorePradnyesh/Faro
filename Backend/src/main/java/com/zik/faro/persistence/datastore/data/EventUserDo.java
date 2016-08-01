package com.zik.faro.persistence.datastore.data;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.data.user.EventInviteStatus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
public class EventUserDo {
    //https://code.google.com/p/gwt-gae-book/wiki/StoringData#Many-to-Many_relationships
	@Id
    private String id;
    @Index
    private Ref<EventDo> eventRef;
    @Index
    private Ref<FaroUserDo> faroUserRef;
    private EventInviteStatus inviteStatus;
    private String ownerId;

    private EventUserDo(){    // To satisfy JAXB
        
    }

   public static EventUserDo createEventUserDoAccepted(final String eventId, final String faroUserId, String ownerId)
    {
        return new EventUserDo(eventId, faroUserId, ownerId, EventInviteStatus.ACCEPTED);
    }

    public EventUserDo(final String eventId, final String faroUserId){
        this.id = generateEventUserId(eventId, faroUserId);
        this.eventRef = Ref.create(Key.create(EventDo.class, eventId));
        this.faroUserRef = Ref.create(Key.create(FaroUserDo.class, faroUserId));
    }

    public EventUserDo(final String eventId, final String faroUserId, final String ownerId, EventInviteStatus inviteStatus){
        this(eventId,faroUserId);
        this.ownerId = ownerId;
        this.inviteStatus = inviteStatus;
    }

    public EventUserDo(final String eventId, final String faroUserId, final String ownerId){
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

    public Ref<EventDo> getEventRef() {
        return eventRef;
    }

    public Ref<FaroUserDo> getFaroUserRef() {
        return faroUserRef;
    }

    public EventDo getEvent(){
        return this.eventRef.get();
    }

    public FaroUserDo getFaroUser(){
        return this.faroUserRef.get();
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
