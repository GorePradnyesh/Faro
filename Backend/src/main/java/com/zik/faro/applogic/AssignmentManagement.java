package com.zik.faro.applogic;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.Item;
import com.zik.faro.notifications.handler.AssignmentNotificationHandler;
import com.zik.faro.persistence.datastore.AssignmentDatastoreImpl;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class AssignmentManagement {
	
	private  static final Logger logger = LoggerFactory.getLogger(AssignmentManagement.class);
	private static AssignmentNotificationHandler assignmentNotificationHandler = new AssignmentNotificationHandler();
	
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
	
	public static Activity updateActivityItems(final String eventId, final String activityId, final List<Item> items, String userId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		createItemIds(items);
		ActivityDo activityDo = AssignmentDatastoreImpl.updateItemsForActivityAssignment(eventId, activityId, items);
		EventDo eventDo = EventDatastoreImpl.loadEventByID(eventId);
		sendBatchNotifications(items, eventDo, activityDo, userId);
		return ConversionUtils.fromDo(activityDo);
	}
	
	public static Event updateEventItems(final String eventId, final List<Item> items, String userId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		createItemIds(items);
		EventDo eventDo = AssignmentDatastoreImpl.updateItemsForEventAssignment(eventId, items);
		sendBatchNotifications(items, eventDo, null, userId);
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
	
	public static void sendBatchNotifications(final List<Item> items, EventDo eventDo, ActivityDo activityDo, String notificationTriggererUserId) throws DataNotFoundException{
		Set<String> faroUserIds = new HashSet<String>();
		// Find all users who need to be notified. Set will ensure batching for a user
		for(Item item : items){
			String assigneeId = item.getAssigneeId();
			if(assigneeId != null && !assigneeId.isEmpty()){
				faroUserIds.add(item.getAssigneeId());
			}else{
				logger.info(MessageFormat.format("FaroUserId is null/empty. TODO ItemId: {0}", item.getId()));
			}
		}
		for(String faroUserId : faroUserIds){
			assignmentNotificationHandler.assignmentCreatedNotification(eventDo, activityDo, faroUserId, notificationTriggererUserId);
		}
	}
}
