package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.BaseEntityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

public class AssignmentDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(AssignmentDatastoreImpl.class);
    
	// Returning Map<Identifier,Assignment>
	// where identifier is activityId for all activity assignments
	// and identifier is eventId for event assignment
	public static Map<String,Assignment> getAllAssignments(final String eventId) throws DataNotFoundException{
		// Returns assignments of all activities in an event
		List<ActivityDo> activities = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		Map<String,Assignment> assignmentMap = new HashMap<String, Assignment>();
		List<Assignment> assignments = new ArrayList<Assignment>();
		for(ActivityDo activity : activities){
			assignmentMap.put(activity.getId(), activity.getAssignment());
		}
		// Add Event level assignment as well.
		assignmentMap.put(eventId, EventDatastoreImpl.loadEventByID(eventId).getAssignment());
		return assignmentMap;
	}
	
	public static Assignment getEventAssignment(final String eventId) throws DataNotFoundException{
		return EventDatastoreImpl.loadEventByID(eventId).getAssignment();
	}
	
	public static Assignment getActivityAssignment(final String eventId, final String activityId, 
			String assignmentId) throws DataNotFoundException{
		ActivityDo activity = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
		if(activity.getAssignment().getId().equals(assignmentId)){
			return activity.getAssignment();
		}
		throw new DataNotFoundException("No data found for assignmentId:" + assignmentId);
	}
	
	public static int getCountOfPendingAssignments(String eventId) throws DataNotFoundException{
		List<ActivityDo> activities = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		int count = 0;
		for(ActivityDo activity : activities){
			count += getPendingCount(activity.getAssignment());
		}
		// Add event level assignment tasks as well
		count += getPendingCount(EventDatastoreImpl.loadEventByID(eventId).getAssignment());
		return count;
	}
	
	private static int getPendingCount(Assignment assignment){
		if(assignment == null){
			return 0;
		}
		List<Item> items = assignment.getItems();
		int count = 0;
		for(Item item : items){
			if(item.getStatus().equals(ActionStatus.INCOMPLETE)){
				count++;
			}
		}
		return count;
	}
	
	public static ActivityDo updateActivityAssignmentItems(final List<Item> toBeAdded, final List<Item> toBeRemoved, 
    		final String activityId, final String eventId, final Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<ActivityDo>>() {
	        public TransactionResult<ActivityDo> run() {
	        	// Read from datastore
	        	ActivityDo activityDo = null;
				try {
					activityDo = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
					logger.info("Activity loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<ActivityDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, activityDo.getVersion())){
					return new TransactionResult<ActivityDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+activityDo.getVersion().toString());
				}
				
				if(toBeRemoved != null)
					activityDo.getAssignment().getItems().removeAll(toBeRemoved);
				
				if(toBeAdded != null)
					addAll(activityDo.getAssignment().getItems(), toBeAdded);
				
				BaseDatastoreImpl.versionIncrement(activityDo);

	            ActivityDatastoreImpl.storeActivity(activityDo);
				
	            return new TransactionResult<ActivityDo>(activityDo, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<ActivityDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Poll updated!");
    	return result.getEntity();
    }
	
	public static void addAll(List<Item> existing, List<Item> toBeAdded){
		for(Item item : toBeAdded){
			int itemIndex = existing.indexOf(item);
		    if (itemIndex != -1) {
		    	existing.set(itemIndex, item);
		    }else{
		    	existing.add(item);
		    }
		}
	}
	
	public static EventDo updateEventAssignmentItems(final List<Item> toBeAdded, final List<Item> toBeRemoved, 
    		final String eventId, final Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<EventDo>>() {
	        public TransactionResult<EventDo> run() {
	        	// Read from datastore
	        	EventDo eventDo = null;
				try {
					eventDo = EventDatastoreImpl.loadEventByID(eventId);
					logger.info("Event loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, eventDo.getVersion())){
					return new TransactionResult<EventDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+eventDo.getVersion().toString());
				}
				
				if(toBeRemoved != null)
					eventDo.getAssignment().getItems().removeAll(toBeRemoved);
				if(toBeAdded != null)
					addAll(eventDo.getAssignment().getItems(), toBeAdded);
				
				BaseDatastoreImpl.versionIncrement(eventDo);

	            EventDatastoreImpl.storeEventOnly(eventDo);
				
	            return new TransactionResult<EventDo>(eventDo, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<EventDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Event updated!");
    	return result.getEntity();
    }
}
