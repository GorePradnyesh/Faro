package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;

import java.util.List;

public class PollDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storePoll(final Poll poll){
        //TODO: Should we ensure that eventId exists before storing the Activity for that EventID
        DatastoreObjectifyDAL.storeObject(poll);
    }

    public static Poll loadPollById(final String pollId, final String eventId) throws DataNotFoundException{
        Poll poll = DatastoreObjectifyDAL.loadObjectWithParentId(Event.class, eventId, Poll.class, pollId);
        return poll;
    }

    public static List<Poll> loadPollsByEventId(final String eventId){
        List<Poll> pollList = DatastoreObjectifyDAL.loadObjectsByAncestorRef(Event.class, eventId, Poll.class);
        return pollList;
    }
}
