package com.zik.faro.applogic;

import java.util.List;
import java.util.Set;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.Poll;
import com.zik.faro.persistence.datastore.PollDatastoreImpl;

public class PollManagement {
	public static void createPoll(final Poll poll) throws DataNotFoundException{
		PollDatastoreImpl.storePoll(poll);
	}
	
	public static List<Poll> getPolls(final String eventId){
		return PollDatastoreImpl.loadPollsByEventId(eventId);
	}
	
	public static Poll getPoll(final String eventId, final String pollId) throws DataNotFoundException{
		return PollDatastoreImpl.loadPollById(pollId, eventId);
	}
	
	public static int getCountOfUnvotedPolls(final String userId, final String eventId){
		return PollDatastoreImpl.getCountofUnvotedPolls(eventId, userId);
	}
	
	public static void castVote(final String eventId, final String pollId,
    		final Set<String> options, final String userId ) throws DatastoreException, DataNotFoundException{
		PollDatastoreImpl.castVote(eventId, pollId, options, userId);
	}
	
	public static void deletePoll(final String eventId, final String pollId){
		PollDatastoreImpl.deletePoll(eventId, pollId);
	}
	
}
