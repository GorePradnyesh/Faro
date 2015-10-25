package com.zik.faro.frontend;

import com.zik.faro.frontend.data.ObjectStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Poll {
    private String pollId;
    private String eventId;
    private String creator;
    private boolean multiChoice = false;

    private List<PollOption> pollOptions = new ArrayList<>();
    private String winnerId;
    private String owner;
    private String description;
    private ObjectStatus status;
    private Calendar deadline;                // Will not be used in V1.

    /* Getters and setters*/
    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isMultiChoice() {
        return multiChoice;
    }

    public void setMultiChoice(boolean multiChoice) {
        this.multiChoice = multiChoice;
    }

    public List<PollOption> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<PollOption> pollOptions) {
        this.pollOptions = pollOptions;
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




    public static class PollOption{
        private String id;                                 //TODO: Change type to Id

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOptionDescription() {
            return optionDescription;
        }

        public void setOptionDescription(String optionDescription) {
            this.optionDescription = optionDescription;
        }

        public List<String> getVoters() {
            return voters;
        }

        public void setVoters(List<String> voters) {
            this.voters = voters;
        }

        public String optionDescription;
        public List<String> voters = new ArrayList<>();   //TODO: Change type to List<Id>/List<MinUser>?

        public PollOption(String option) {
            this.optionDescription = option;
        }
    }

    public Poll(String eventId, String creator, boolean multiChoice,
                List<PollOption> pollOptions, String owner, String description,
                ObjectStatus status) {
        this.eventId = eventId;
        this.creator = creator;
        this.multiChoice = multiChoice;
        this.pollOptions = pollOptions;
        this.owner = owner;
        this.description = description;
        this.status = status;
    }
}