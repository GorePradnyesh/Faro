package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.notifications.handler.ActivityNotificationHandler;
import com.zik.faro.persistence.datastore.ActivityDatastoreImpl;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

public class ActivityManagement {
	public static ActivityNotificationHandler activityNotificationHandler = new ActivityNotificationHandler();
	
	public static Activity createActivity(final Activity activity, String userId){
		ActivityDo activityDo = new ActivityDo(activity.getEventId(), 
				activity.getName(), activity.getDescription(), activity.getLocation(), 
				activity.getStartDate(), activity.getEndDate(), new Assignment());
		ActivityDatastoreImpl.storeActivity(activityDo);
		EventDo eventDo = activityDo.getEventRef().get();
		activityNotificationHandler.createActivityNotification(activityDo, eventDo, userId);
		return ConversionUtils.fromDo(activityDo);
	}
	
	public static List<Activity> getActivities(String eventId){
		List<ActivityDo> activityDoList = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		List<Activity>  activities = new ArrayList<Activity>();
		if(activityDoList != null && activityDoList.size() > 0){
			for(ActivityDo activityDo : activityDoList){
				activities.add(ConversionUtils.fromDo(activityDo));
			}
		}
		return activities;
	}
	
	public static Activity getActivity(String eventId, String activityId) throws DataNotFoundException{
		return ConversionUtils.fromDo(ActivityDatastoreImpl.loadActivityById(activityId, eventId));
	}
	
	public static void deleteActivity(String eventId, String activityId, String userId) throws DataNotFoundException{
		ActivityDo activityDo = ActivityDatastoreImpl.loadActivityById(activityId, eventId);
		ActivityDatastoreImpl.deleteActivityById(activityId, eventId);
		activityNotificationHandler.deleteActivityNotification(activityDo, activityDo.getEventRef().get(), userId);
	}
	
	public static Activity updateActivity(Activity updateActivity, String eventId, String userId, Set<String> updatedFields) throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
		ActivityDo updateActivityDo = ConversionUtils.toDo(updateActivity);
		ActivityDo updated = ActivityDatastoreImpl.updateActivity(updateActivityDo, eventId, updatedFields);
		EventDo eventDo = updated.getEventRef().get();
		activityNotificationHandler.updateActivityNotification(updateActivityDo, eventDo, userId);
		return ConversionUtils.fromDo(updated);
	}
	
	
}
