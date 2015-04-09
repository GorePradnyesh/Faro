package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;

public class AssignmentDatastoreImpl {
	
	public static List<Assignment> getAllAssignments(final String eventId) throws DataNotFoundException{
		// Returns assignments of all activities in an event
		List<Activity> activities = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		List<Assignment> assignments = new ArrayList<Assignment>();
		for(Activity activity : activities){
			assignments.add(activity.getAssignment());
		}
		// Add Event level assignment as well.
		assignments.add(EventDatastoreImpl.loadEventByID(eventId).getAssignment());
		return assignments;
	}
	
	public static Assignment getEventAssignment(final String eventId) throws DataNotFoundException{
		return EventDatastoreImpl.loadEventByID(eventId).getAssignment();
	}
	
	public static Assignment getActivityAssignment(final String eventId, final String activityId, 
			String assignmentId) throws DataNotFoundException{
		Activity activity = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
		if(activity.getAssignment().id.equals(assignmentId)){
			return activity.getAssignment();
		}
		throw new DataNotFoundException("No data found for assignmentId:" + assignmentId);
	}
	
	public static int getCountOfPendingAssignments(String eventId) throws DataNotFoundException{
		List<Activity> activities = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		int count = 0;
		for(Activity activity : activities){
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
			final List<Item> items) throws DataNotFoundException{
		Work w = new Work<TransactionResult>() {			
			@Override
			public TransactionResult run() {
				// Read from datastore
	        	Activity activity;
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
		//TODO: this if block used in multiple places. Not cool.
		if(result.equals(TransactionResult.DATANOTFOUND)){
    		//TODO change enum to send message as well to extract key of entity
    		throw new DataNotFoundException("");
    	}
	}
	
	public static void updateItemsForEventAssignment(final String eventId, final List<Item> items) throws DataNotFoundException{
		Work w = new Work<TransactionResult>() {
			
			@Override
			public TransactionResult run() {
				// Read from datastore
	        	Event event;
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
	            EventDatastoreImpl.storeEvent(event);
	            return TransactionResult.SUCCESS;
			}
		};
		TransactionResult result = DatastoreObjectifyDAL.update(w);
		if(result.equals(TransactionResult.DATANOTFOUND)){
    		//TODO change enum to send message as well to extract key of entity
    		throw new DataNotFoundException("");
    	}
	}
}
