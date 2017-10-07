package com.zik.faro.persistence.datastore;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.PollOption;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;

public class PollDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(PollDatastoreImpl.class);
    
	private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storePoll(final PollDo pollDo) throws DataNotFoundException{
        // Load event. If not present it will throw Exception and hence will not proceed
    	// to create poll for absent event
    	EventDatastoreImpl.loadEventByID(pollDo.getEventId());
    	DatastoreObjectifyDAL.storeObject(pollDo);
    	logger.info("Poll created");
    }

    public static PollDo loadPollById(final String pollId, final String eventId) throws DataNotFoundException{
        PollDo poll = DatastoreObjectifyDAL.loadObjectWithParentId(EventDo.class, eventId, PollDo.class, pollId);
        logger.info("Poll loaded by pollId");
        return poll;
    }

    public static List<PollDo> loadPollsByEventId(final String eventId){
        List<PollDo> pollList = DatastoreObjectifyDAL.loadObjectsByAncestorRef(EventDo.class, eventId, PollDo.class);
        logger.info("Total polls fetched given eventId:"+(pollList == null ? 0 : pollList.size()));
        return pollList;
    }
    
    public static int getCountofUnvotedPolls(final String eventId, final String userId){
    	List<PollDo> polls = loadPollsByEventId(eventId);
    	logger.info("Total polls fetched given eventId:"+(polls == null ? 0 : polls.size()));
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
    	logger.info("Total count of unvoted polls:"+count);
    	return count;
    }
    
    public static PollDo castVote(final Set<String> addOptions, final Set<String> removeOptions, 
    		final String pollId, final String eventId, final Long version, final String userId) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<PollDo>>() {
	        public TransactionResult<PollDo> run() {
	        	// Read from datastore
	        	PollDo poll = null;
				try {
					poll = loadPollById(pollId, eventId);
					logger.info("Poll loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, poll.getVersion())){
					return new TransactionResult<PollDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+poll.getVersion().toString());
				}
				
				if(removeOptions != null){
					for(PollOption p : poll.getPollOptions()){
						if(removeOptions.contains(p.getId())){
							p.getVoters().remove(userId);
						}
					}
				}
				
				if(addOptions != null){
					for(PollOption p : poll.getPollOptions()){
						if(addOptions.contains(p.getId())){
							p.getVoters().add(userId);
						}
					}
				}
				
				BaseDatastoreImpl.versionIncrement(poll);

	            // Store
	            try {
					storePoll(poll);
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
	            return new TransactionResult<PollDo>(poll, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<PollDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Poll updated!");
    	return result.getEntity();
    }
    
    public static PollDo updatePollOptions(final List<PollOption> addOptions, final List<PollOption> removeOptions, 
    		final String pollId, final String eventId, final Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<PollDo>>() {
	        public TransactionResult<PollDo> run() {
	        	// Read from datastore
	        	PollDo poll = null;
				try {
					poll = loadPollById(pollId, eventId);
					logger.info("Poll loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, poll.getVersion())){
					return new TransactionResult<PollDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+poll.getVersion().toString());
				}
				
				if(removeOptions != null)
					poll.getPollOptions().removeAll(removeOptions);
				if(addOptions != null)
					poll.getPollOptions().addAll(addOptions);
				
				BaseDatastoreImpl.versionIncrement(poll);

	            // Store
	            try {
					storePoll(poll);
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
	            return new TransactionResult<PollDo>(poll, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<PollDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Poll updated!");
    	return result.getEntity();
    }
    
    public static PollDo updatePoll(final PollDo updatedPoll, final Set<String> updatedFields) 
    			 throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<PollDo>>() {
	        public TransactionResult<PollDo> run() {
	        	// Read from datastore
	        	PollDo poll = null;
				try {
					poll = loadPollById(updatedPoll.getId(), updatedPoll.getEventId());
					logger.info("Poll loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(updatedPoll, poll)){
					return new TransactionResult<PollDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+poll.getVersion().toString());
				}
				
				try {
					BaseDatastoreImpl.updateModifiedFields(poll, updatedPoll, updatedFields);
				} catch (Exception e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.UPDATEEXCEPTION, "Cannot apply update delta");
				}
				BaseDatastoreImpl.versionIncrement(updatedPoll, poll);

	            // Store
	            try {
					storePoll(poll);
				} catch (DataNotFoundException e) {
					return new TransactionResult<PollDo>(null, TransactionStatus.DATANOTFOUND);
				}
	            return new TransactionResult<PollDo>(poll, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<PollDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Poll updated!");
    	return result.getEntity();
    }
    
    public static void deletePoll(final String eventId, final String pollId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(pollId, PollDo.class,
    			eventId, EventDo.class);
    	logger.info("Poll deleted");
    }
    
}
