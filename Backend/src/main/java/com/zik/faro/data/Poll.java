package com.zik.faro.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Poll {
    private String id;
    private String eventId;
    private String creatorId;

    private List<PollOption> pollOptions = new ArrayList<>();
    private String winnerId;
    private String owner;
    private String description;
    private ObjectStatus status;
    private Calendar deadline;                // Will not be used in V1.
    private boolean multiChoice = false;

    public Poll(){ //to satisfy jaxb;
    }

    public Poll(String eventId, String creator, List<PollOption> pollOptions, String owner, String description) {
    	this(null,eventId, creator, pollOptions,
    			owner, description);
    }
    
    public Poll(String id, String eventId, String creator, List<PollOption> pollOptions, String owner, String description){
    	this.id = id;
    	this.eventId = eventId;
        this.creatorId = creator;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = ObjectStatus.OPEN;
    }

    public Poll(String eventId, String creator, boolean multiChoice,
                List<PollOption> pollOptions, String owner, String description,
                ObjectStatus status) {
        this.eventId = eventId;
        this.creatorId = creator;
        this.multiChoice = multiChoice;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
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
    	this.eventId = eventId;
    }
    
    public void setPollOptions(List<PollOption> pollOptions) {
		this.pollOptions = pollOptions;
	}

    public boolean isMultiChoice() {
        return multiChoice;
    }

    public void setMultiChoice(boolean multiChoice) {
        this.multiChoice = multiChoice;
    }
}