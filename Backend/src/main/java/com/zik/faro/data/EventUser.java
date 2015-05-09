package com.zik.faro.data;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.InviteStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
@Entity
public class EventUser {
    //https://code.google.com/p/gwt-gae-book/wiki/StoringData#Many-to-Many_relationships
	@Id
    private String id;
    @Index
    private Ref<Event> eventRef;
    @Index
    private Ref<FaroUser> faroUserRef;
    private InviteStatus inviteStatus;
    private String ownerId;

    private EventUser(){    // To satisfy JAXB
        
    }

    public EventUser(final String eventId, final String faroUserId){
        this.id = generateEventUserId(eventId, faroUserId);
        this.eventRef = Ref.create(Key.create(Event.class, eventId));
        this.faroUserRef = Ref.create(Key.create(FaroUser.class, faroUserId));
        this.inviteStatus = InviteStatus.INVITED;
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

    public Ref<Event> getEventRef() {
        return eventRef;
    }

    public Ref<FaroUser> getFaroUserRef() {
        return faroUserRef;
    }

    public Event getEvent(){
        return this.eventRef.get();
    }

    public FaroUser getFaroUser(){
        return this.faroUserRef.get();
    }

    public InviteStatus getInviteStatus(){
        return this.inviteStatus;
    }

    public void setAccepted(){
        this.inviteStatus = InviteStatus.ACCEPTED;
    }

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
}
