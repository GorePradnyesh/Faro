package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zik.faro.data.Poll;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.PollDo;
import com.zik.faro.persistence.datastore.PollDatastoreImpl;

public class PollManagement {
	public static Poll createPoll(final Poll poll) throws DataNotFoundException{
		PollDo pollDo = new PollDo(poll.getEventId(), poll.getCreatorId(), poll.getPollOptions(),
				poll.getOwner(), poll.getDescription());
		PollDatastoreImpl.storePoll(pollDo);
		return ConversionUtils.fromDo(pollDo);
	}
	
	public static List<Poll> getPolls(final String eventId){
		List<PollDo> pollDos = PollDatastoreImpl.loadPollsByEventId(eventId);
		List<Poll> polls = new ArrayList<Poll>(pollDos.size());
		for(PollDo pollDo : pollDos){
			polls.add(ConversionUtils.fromDo(pollDo));
		}
		return polls;
	}
	
	public static Poll getPoll(final String eventId, final String pollId) throws DataNotFoundException{
		return ConversionUtils.fromDo(PollDatastoreImpl.loadPollById(pollId, eventId));
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
