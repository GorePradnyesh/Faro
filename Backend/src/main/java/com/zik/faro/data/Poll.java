package com.zik.faro.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
@XmlRootElement
public class Poll {
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

    private Poll(){ //to satisfy jaxb;
        System.out.println();
    }

    public Poll(String eventId, String creator, List<PollOption> pollOptions, String owner, String description) {
    	this(UUID.randomUUID().toString(),eventId, creator, pollOptions,
    			owner, description);
    }
    
    public Poll(String id, String eventId, String creator, List<PollOption> pollOptions, String owner, String description){
    	this.id = id;
    	this.eventId = Ref.create(Key.create(EventDo.class, eventId));
        this.creatorId = creator;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = ObjectStatus.OPEN;
    }


    @XmlRootElement
    public static class PollOption{
        public String id;                                 
        public String option;
        public Set<String> voters = new HashSet<String>();   

        private PollOption(){
            
        }

        public PollOption(final String option){
            this.id = UUID.randomUUID().toString();
            this.option = option;
        }

        public List<String> getVoters(){
        	return Arrays.asList((String[])this.voters.toArray());                              
        }

        public void addVoters(final String voterId){
            this.voters.add(voterId);
        }
        
        public void setVoters(final Set<String> voters){
        	this.voters = voters;
        }
    }

    @XmlElement
    public String getId() {
        return id;
    }

    @XmlElement
    public String getEventId() {
        return eventId.getKey().getName();
    }

    @XmlElement
    public String getCreatorId() {
        return creatorId;
    }

    @XmlElement
    public List<PollOption> getPollOptions(){
        return this.pollOptions;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
        this.status = ObjectStatus.CLOSED;
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
    
   
}

