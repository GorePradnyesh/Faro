package com.zik.faro.persistence.datastore;

import com.zik.faro.data.Poll;

import java.util.HashMap;
import java.util.Map;

public class PollDatastoreImpl {
    public static void storePoll(final Poll poll){
        DatastoreObjectifyDAL.storeObject(poll);
    }

    public static void loadPollById(final String pollId, final String eventId){
        Map<DatastoreOperator, String> filterKeyMap = new HashMap<>();
        filterKeyMap.put(DatastoreOperator.EQ, pollId);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("eventId", eventId);
    }
}
