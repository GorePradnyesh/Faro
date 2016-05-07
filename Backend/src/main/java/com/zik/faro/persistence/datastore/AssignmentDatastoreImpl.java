package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.data.Assignment;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.data.Item;

public class AssignmentDatastoreImpl {
	
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
	
	public static void updateItemsForActivityAssignment(final String eventId, final String activityId,
			final List<Item> items) throws DataNotFoundException, DatastoreException{
		Work w = new Work<TransactionResult>() {			
			@Override
			public TransactionResult run() {
				// Read from datastore
	        	ActivityDo activity;
				try {
					activity = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
	        	
	            // Modify.
	            for(Item item: items){
	            	activity.getAssignment().addItem(item);
	            }
	            
	            // Store
	            ActivityDatastoreImpl.storeActivity(activity);
	            return TransactionResult.SUCCESS;
	        }
		};
		TransactionResult result = DatastoreObjectifyDAL.update(w);
		DatastoreUtil.processResult(result);
	}
	
	public static void updateItemsForEventAssignment(final String eventId, final List<Item> items) throws DataNotFoundException, DatastoreException{
		Work w = new Work<TransactionResult>() {
			
			@Override
			public TransactionResult run() {
				// Read from datastore
	        	EventDo event;
				try {
					event = EventDatastoreImpl.loadEventByID(eventId);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
	        	
	            // Modify.
	            for(Item item: items){
	            	event.getAssignment().addItem(item);
	            }
	            
	            // Store
	            EventDatastoreImpl.storeEventOnly(event);
	            return TransactionResult.SUCCESS;
			}
		};
		TransactionResult result = DatastoreObjectifyDAL.update(w);
		DatastoreUtil.processResult(result);
	}
}
