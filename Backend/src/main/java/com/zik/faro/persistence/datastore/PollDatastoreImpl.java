package com.zik.faro.persistence.datastore;

import java.util.List;
import java.util.Set;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.PollOption;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;

public class PollDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storePoll(final PollDo pollDo) throws DataNotFoundException{
        // Load event. If not present it will throw Exception and hence will not proceed
    	// to create poll for absent event
    	EventDatastoreImpl.loadEventByID(pollDo.getEventId());
    	DatastoreObjectifyDAL.storeObject(pollDo);
    }

    public static PollDo loadPollById(final String pollId, final String eventId) throws DataNotFoundException{
        PollDo poll = DatastoreObjectifyDAL.loadObjectWithParentId(EventDo.class, eventId, PollDo.class, pollId);
        return poll;
    }

    public static List<PollDo> loadPollsByEventId(final String eventId){
        List<PollDo> pollList = DatastoreObjectifyDAL.loadObjectsByAncestorRef(EventDo.class, eventId, PollDo.class);
        return pollList;
    }
    
    public static int getCountofUnvotedPolls(final String eventId, final String userId){
    	List<PollDo> polls = loadPollsByEventId(eventId);
    	int count = 0;
    	for(PollDo p : polls){
    		List<PollOption> options = p.getPollOptions();
    		boolean found = false;
    		for(PollOption option : options){
    			// Check if user has voted for atleast one option of a poll.
    			// Dont care if he has voted for multiple options of the poll;
    			if(option.getVoters().contains(userId)){
    				found = true;
    				break;
    			}
    		}
    		if(!found){
    			count++;
    		}
    	}
    	return count;
    }
    
    public static PollDo castVote(final String eventId, final String pollId,
    		final Set<String> options, final String userId) throws DatastoreException, DataNotFoundException, UpdateVersionException{
    	Work w = new Work<TransactionResult<PollDo>>() {
    		
			@Override
			public TransactionResult<PollDo> run(){
				PollDo poll;
				try {
					poll = loadPollById(pollId, eventId);
					
					// Iterate over all poll options. 
					// Since it is a list I cannot get O(1) operation either ways.
					for(PollOption pollOption : poll.getPollOptions()){
						if(options.contains(pollOption.getId())){
							pollOption.getVoters().add(userId);
						}else{
							pollOption.getVoters().remove(userId);
						}
					}
	                
	                storePoll(poll);
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
				return new TransactionResult<PollDo>(poll, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<PollDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        return result.getEntity();
    }
    
    public static PollDo updatePoll(final String eventId, final String pollId,
    		final PollDo updatePoll, final String userId) throws DatastoreException, DataNotFoundException, UpdateVersionException{
    	Work w = new Work<TransactionResult<PollDo>>() {
    		
			@Override
			public TransactionResult<PollDo> run(){
				PollDo poll;
				try {
					poll = loadPollById(pollId, eventId);
					
					if(updatePoll.getCreatorId() != null){
						poll.setCreatorId(updatePoll.getCreatorId());
					}
					
					if(updatePoll.getDeadline() != null){
						poll.setDeadline(updatePoll.getDeadline());
					}
					
					if(updatePoll.getDescription() != null){
						poll.setDescription(updatePoll.getDescription());
					}
					
					if(updatePoll.getOwner() != null){
						poll.setOwner(updatePoll.getOwner());
					}
					
					if(updatePoll.getPollOptions() != null && !updatePoll.getPollOptions().isEmpty()){
						for(PollOption option: updatePoll.getPollOptions()){
							poll.addPollOptions(option);
						}
					}
					
					if(updatePoll.getStatus() != null){
						poll.setStatus(updatePoll.getStatus());
					}
					
					if(updatePoll.getWinnerId() != null){
						poll.setWinnerId(updatePoll.getWinnerId());
					}
	                
	                storePoll(poll);
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
				return new TransactionResult<PollDo>(poll, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<PollDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        return result.getEntity();
    }
    
    public static void deletePoll(final String eventId, final String pollId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(pollId, PollDo.class,
    			eventId, EventDo.class);
    }
    
}
