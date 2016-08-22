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
	
	public static ActivityDo updateItemsForActivityAssignment(final String eventId, final String activityId,
			final List<Item> items) throws DataNotFoundException, DatastoreException{
		Work w = new Work<TransactionResult<ActivityDo>>() {			
			@Override
			public TransactionResult<ActivityDo> run() {
				// Read from datastore
	        	ActivityDo activity;
				try {
					activity = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
				} catch (DataNotFoundException e) {
					return new TransactionResult<ActivityDo>(null, TransactionStatus.DATANOTFOUND);
				}
	        	
	            // Modify.
				updateItemsInExistingList(items, activity.getAssignment().getItems());
	            
	            // Store
	            ActivityDatastoreImpl.storeActivity(activity);
	            return new TransactionResult<ActivityDo>(activity, TransactionStatus.SUCCESS);
	        }
		};
		TransactionResult<ActivityDo> result = DatastoreObjectifyDAL.update(w);
		DatastoreUtil.processResult(result);
		return result.getEntity();
	}
	
	public static EventDo updateItemsForEventAssignment(final String eventId, final List<Item> items) throws DataNotFoundException, DatastoreException{
		Work w = new Work<TransactionResult<EventDo>>() {
			
			@Override
			public TransactionResult<EventDo> run() {
				// Read from datastore
	        	EventDo event;
				try {
					event = EventDatastoreImpl.loadEventByID(eventId);
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.DATANOTFOUND);
				}
	        	
	            // Modify.
				updateItemsInExistingList(items, event.getAssignment().getItems());
	            
	            // Store
	            EventDatastoreImpl.storeEventOnly(event);
	            return new TransactionResult<EventDo>(event, TransactionStatus.SUCCESS);
			}
		};
		TransactionResult<EventDo> result = DatastoreObjectifyDAL.update(w);
		DatastoreUtil.processResult(result);
		return result.getEntity();
	}
	
	private static void updateItemsInExistingList(List<Item> newList, List<Item> existingList){
		if(newList == null || newList.isEmpty()){
			return;
		}
		// O(m*n) complexity, where m is number of new items to be updated and n is existing items in todo list
		// TODO: Revisit and see if hashmap possible.
		for(Item item: newList){
        	int i = 0;
        	boolean removeItem = false;
        	for( ;i < existingList.size(); i++){
        		if(existingList.get(i).getId().equals(item.getId())){
        			removeItem = true;
        			break;
        		}
        	}
        	if(removeItem){
        		existingList.remove(i+1);
        	}
        	existingList.add(item);
        }
	}
}
