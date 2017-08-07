package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;

import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.notifications.handler.PollNotificationHandler;
import com.zik.faro.persistence.datastore.PollDatastoreImpl;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;

public class PollManagement {
	private static ObjectMapper mapper = new ObjectMapper();
	public static PollNotificationHandler pollNotificationHandler = new PollNotificationHandler();
	
	public static Poll createPoll(final Poll poll, final String userId) throws DataNotFoundException{
		// Create option ids during poll creation
		generatePollIds(poll.getPollOptions());
		PollDo pollDo = new PollDo(poll.getEventId(), poll.getCreatorId(), poll.getPollOptions(), poll.getWinnerId(), 
				poll.getOwner(), poll.getDescription(), poll.getStatus(), poll.getDeadline(), poll.getMultiChoice());
		PollDatastoreImpl.storePoll(pollDo);
		EventDo eventDo = pollDo.getEventRef().get();
		pollNotificationHandler.createPollNotification(pollDo, eventDo, userId);
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
	
	public static Poll update(final String eventId, final String pollId,
    		final Map<String, Object> updateObj, final String userId ) throws DatastoreException, DataNotFoundException, UpdateVersionException{
		Poll poll = extractPollUpdateObject(updateObj);
		Set<String> voteOptions = extractVoteOptionsUpdateObject(updateObj);
		generatePollIds(poll.getPollOptions());
		PollDo pollDo = ConversionUtils.toDo(poll);
		pollDo = PollDatastoreImpl.updatePoll(eventId, pollId, pollDo, userId, voteOptions);
		pollNotificationHandler.updatePollNotification(pollDo, pollDo.getEventRef().get(), userId);
		return ConversionUtils.fromDo(pollDo);
	}
	
	public static Poll extractPollUpdateObject(Map<String, Object> updateObj){
		if(updateObj != null && updateObj.containsKey("poll")){
			
	        try {
	        	JSONObject jObj = new JSONObject(updateObj.get("poll").toString());
				return mapper.readValue(jObj.toString(), Poll.class);
			} catch (Exception e) {
				throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "Incorrect update object for Poll");
			}
		}
		throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "Update object missing for Poll");
	}
	
	public static Set<String> extractVoteOptionsUpdateObject(Map<String, Object> updateObj){
		if(updateObj != null && updateObj.get("voteOption") != null){
			return new HashSet<String>((List<String>) updateObj.get("voteOption"));
		}
		return null;
	}
	
	public static void deletePoll(final String eventId, final String pollId, final String userId) throws DataNotFoundException{
		PollDo pollDo = PollDatastoreImpl.loadPollById(pollId, eventId);
		PollDatastoreImpl.deletePoll(eventId, pollId);
		pollNotificationHandler.deletePollNotification(pollDo, pollDo.getEventRef().get(), userId);
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
