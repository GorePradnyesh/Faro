package com.zik.faro.persistence.datastore;

import java.util.List;
import java.util.Set;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.Poll.PollOption;

public class PollDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storePoll(final Poll poll) throws DataNotFoundException{
        // Load event. If not present it will throw Exception and hence will not proceed
    	// to create poll for absent event
    	EventDatastoreImpl.loadEventByID(poll.getEventId());
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
    
    public static int getCountofUnvotedPolls(final String eventId, final String userId){
    	List<Poll> polls = loadPollsByEventId(eventId);
    	int count = 0;
    	for(Poll p : polls){
    		List<PollOption> options = p.getPollOptions();
    		boolean found = false;
    		for(PollOption option : options){
    			// Check if user has voted for atleast one option of a poll.
    			// Dont care if he has voted for multiple options of the poll;
    			if(option.voters.contains(userId)){
    				found = true;
    				break;
    			}
    		}
    		if(found){
    			count++;
    		}
    	}
    	return count;
    }
    
    public static void castVote(final String eventId, final String pollId,
    		final Set<String> options, final String userId) throws DatastoreException, DataNotFoundException{
    	Work w = new Work<TransactionResult>() {
    		
			@Override
			public TransactionResult run(){
				Poll poll;
				try {
					poll = loadPollById(pollId, eventId);
					
					// Iterate over all poll options. 
					// Since it is a list I cannot get O(1) operation either ways.
					for(PollOption pollOption : poll.getPollOptions()){
						if(options.contains(pollOption.id)){
							pollOption.voters.add(userId);
						}
					}
	                
	                storePoll(poll);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
				return TransactionResult.SUCCESS;
			}
		};
        TransactionResult result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
    }
    
    public static void deletePoll(final String eventId, final String pollId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(pollId, Poll.class,
    			eventId, Event.class);
    }
    
}
