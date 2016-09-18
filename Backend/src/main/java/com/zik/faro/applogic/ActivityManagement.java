package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.ActivityDatastoreImpl;

public class ActivityManagement {
	public static Activity createActivity(final Activity activity){
		ActivityDo activityDo = new ActivityDo(activity.getEventId(), 
				activity.getName(), activity.getDescription(), activity.getLocation(), 
				activity.getStartDate(), activity.getEndDate(), new Assignment());
		ActivityDatastoreImpl.storeActivity(activityDo);
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
	
	public static void deteleActivity(String eventId, String activityId){
		ActivityDatastoreImpl.delelteActivityById(activityId, eventId);
	}
	
	public static Activity updateActivity(Activity updateActivity, String eventId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		ActivityDo updateActivityDo = ConversionUtils.toDo(updateActivity);
		return ConversionUtils.fromDo(ActivityDatastoreImpl.updateActivity(updateActivityDo, eventId));
	}
	
	
}
