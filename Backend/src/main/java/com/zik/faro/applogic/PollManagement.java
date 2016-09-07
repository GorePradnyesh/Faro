package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.persistence.datastore.data.PollDo;
import com.zik.faro.persistence.datastore.PollDatastoreImpl;

public class PollManagement {
	public static Poll createPoll(final Poll poll) throws DataNotFoundException{
		// Create option ids during poll creation
		generatePollIds(poll.getPollOptions());
		PollDo pollDo = new PollDo(poll.getEventId(), poll.getCreatorId(), poll.getPollOptions(), poll.getWinnerId(), 
				poll.getOwner(), poll.getDescription(), poll.getStatus(), poll.getDeadline(), poll.getMultiChoice());
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
	
	public static Poll update(final String eventId, final String pollId,
    		final Poll poll, final String userId ) throws DatastoreException, DataNotFoundException{
		generatePollIds(poll.getPollOptions());
		PollDo pollDo = ConversionUtils.toDo(poll);
		return ConversionUtils.fromDo(PollDatastoreImpl.updatePoll(eventId, pollId, pollDo, userId));
	}
	
	public static void deletePoll(final String eventId, final String pollId){
		PollDatastoreImpl.deletePoll(eventId, pollId);
	}
	
	private static void generatePollIds(List<PollOption> pollOptions){
		if(pollOptions != null && !pollOptions.isEmpty()){
			for(PollOption option: pollOptions){
				if(option.getId() == null || option.getId().isEmpty()){
					option.setId(UUID.randomUUID().toString());
				}
			}
		}
	}
	
}
