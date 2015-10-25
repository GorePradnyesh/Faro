package com.zik.faro.applogic;

import java.util.List;
import java.util.Map;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.persistence.datastore.AssignmentDatastoreImpl;

public class AssignmentManagement {
	
	public static int getPendingAssignmentCount(final String eventId) throws DataNotFoundException{
		return AssignmentDatastoreImpl.getCountOfPendingAssignments(eventId);
	}
	
	public static Assignment getActivityLevelAssignment(final String eventId, final String activityId, final String assignmentId) throws DataNotFoundException{
		return AssignmentDatastoreImpl.getActivityAssignment(eventId, activityId, assignmentId);
	}
	
	public static Assignment getEventLevelAssignment(final String eventId) throws DataNotFoundException{
		return AssignmentDatastoreImpl.getEventAssignment(eventId);
	}
	
	public static Map<String,Assignment> getAllAssignments(final String eventId) throws DataNotFoundException{
		return AssignmentDatastoreImpl.getAllAssignments(eventId);
	}
	
	public static void updateActivityItems(final String eventId, final String activityId, final List<Item> items) throws DataNotFoundException, DatastoreException{
		AssignmentDatastoreImpl.updateItemsForActivityAssignment(eventId, activityId, items);
	}
	
	public static void updateEventItems(final String eventId, final List<Item> items) throws DataNotFoundException, DatastoreException{
		AssignmentDatastoreImpl.updateItemsForEventAssignment(eventId, items);
	}
}
