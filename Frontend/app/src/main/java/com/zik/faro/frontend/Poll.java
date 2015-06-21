package com.zik.faro.frontend;

import com.zik.faro.frontend.data.ObjectStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Poll {
    private String id;
    private String eventId;
    private String creator;
    private boolean multiChoice = false;

    private List<PollOption> pollOptions = new ArrayList<>();
    private String winnerId;
    private String owner;
    private String description;
    private ObjectStatus status;
    private Calendar deadline;                // Will not be used in V1.


    public static class PollOption{
/*        private final String id;                                 //TODO: Change type to Id
        public final String option;
        public final List<String> voters = new ArrayList<>();   //TODO: Change type to List<Id>/List<MinUser>?
*/
    }

    public Poll(String id, String eventId, String creator, boolean multiChoice,
                List<PollOption> pollOptions, String winnerId, String owner, String description,
                ObjectStatus status, Calendar deadline) {
        this.id = id;
        this.eventId = eventId;
        this.creator = creator;
        this.multiChoice = multiChoice;
        this.pollOptions = pollOptions;
        this.winnerId = winnerId;
        this.owner = owner;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
    }
}