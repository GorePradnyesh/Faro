package com.zik.faro.applogic;

import java.util.List;
import java.util.Map;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.Item;
import com.zik.faro.persistence.datastore.AssignmentDatastoreImpl;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

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
	
	public static Activity updateActivityItems(final String eventId, final String activityId, final List<Item> items) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		createItemIds(items);
		ActivityDo activityDo = AssignmentDatastoreImpl.updateItemsForActivityAssignment(eventId, activityId, items);
		return ConversionUtils.fromDo(activityDo);
	}
	
	public static Event updateEventItems(final String eventId, final List<Item> items) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		createItemIds(items);
		EventDo eventDo = AssignmentDatastoreImpl.updateItemsForEventAssignment(eventId, items);
		return ConversionUtils.fromDo(eventDo);
	}
	
	public static void createItemIds(final List<Item> items){
		// For new items, create ids
		if(items != null && !items.isEmpty()){
			for(Item item : items){
				if(item.getId() == null || item.getId().isEmpty()){
					item.setId(Identifier.createUniqueIdentifierString());
				}
			}
		}
	}
}
