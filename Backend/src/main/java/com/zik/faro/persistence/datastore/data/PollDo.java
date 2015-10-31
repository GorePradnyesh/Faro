package com.zik.faro.persistence.datastore.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.PollOption;

@Entity
public class PollDo {
    @Id
    private String id;
    @Parent
    private Ref<EventDo> eventId;
    private String creatorId;

    private List<PollOption> pollOptions = new ArrayList<>();
    private String winnerId;
    private String owner;
    private String description;
    private ObjectStatus status;
    private Calendar deadline;                // Will not be used in V1.

    public PollDo(){ //to satisfy jaxb;
    }

    public PollDo(String eventId, String creator, List<PollOption> pollOptions, String owner, String description) {
    	this(UUID.randomUUID().toString(),eventId, creator, pollOptions,
    			owner, description);
    }
    
    public PollDo(String id, String eventId, String creator, List<PollOption> pollOptions, String owner, String description){
    	this.id = id;
    	this.eventId = Ref.create(Key.create(EventDo.class, eventId));
        this.creatorId = creator;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = ObjectStatus.OPEN;
    }

    public String getId() {
        return id;
    }
    
    public String getEventId() {
        return eventId.getKey().getName();
    }
    
    public String getCreatorId() {
        return creatorId;
    }
    
    public List<PollOption> getPollOptions(){
        return this.pollOptions;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }
    
    public void setCreatorId(String creatorId){
    	this.creatorId = creatorId;
    }
    
    public void setId(String id){
    	this.id = id;
    }
    
    public void setEventId(String eventId){
    	this.eventId = Ref.create(Key.create(EventDo.class, eventId));;
    }
    
    public void setEventId(Ref<EventDo> eventId) {
		this.eventId = eventId;
	}

	public void setPollOptions(List<PollOption> pollOptions) {
		this.pollOptions = pollOptions;
	}
}
