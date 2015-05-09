package com.zik.faro.frontend.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Poll {
    private String id;
    private String eventId;
    private String creator;

    private List<PollOption> pollOptions = new ArrayList<>();
    private String winnerId;
    private String owner;
    private String description;
    private ObjectStatus status;
    private DateOffset deadline;                // Will not be used in V1.

    public Poll(String eventId, String creator, List<PollOption> pollOptions, String owner, String description) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.creator = creator;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = ObjectStatus.OPEN;
    }


    public static class PollOption{
        public final String id;                                 //TODO: Change type to Id
        public final String option;
        public final List<String> voters = new ArrayList<>();   //TODO: Change type to List<Id>/List<MinUser>?

        private PollOption(){
            this.id = null; this.option = null; // To satisfy JAXB
        }

        public PollOption(final String option){
            this.id = UUID.randomUUID().toString();
            this.option = option;
        }

        public List<String> getVoters(){
            return this.voters;                                 //TODO: return clone and not the actual reference.
        }

        public void addVoters(final String voterId){
            this.voters.add(voterId);
        }
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCreator() {
        return creator;
    }

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

    public DateOffset getDeadline() {
        return deadline;
    }

    public void setDeadline(DateOffset deadline) {
        this.deadline = deadline;
    }
}
