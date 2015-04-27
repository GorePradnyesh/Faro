package com.zik.faro.applogic;

import java.util.List;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Activity;
import com.zik.faro.persistence.datastore.ActivityDatastoreImpl;

public class ActivityManagement {
	public static void createActivity(final Activity activity){
		ActivityDatastoreImpl.storeActivity(activity);
	}
	
	public static List<Activity> getActivities(String eventId){
		return ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
	}
	
	public static Activity getActivity(String eventId, String activityId) throws DataNotFoundException{
		return ActivityDatastoreImpl.loadActivityById(activityId, eventId);
	}
	
	public static void deteleActivity(String eventId, String activityId){
		ActivityDatastoreImpl.delelteActivityById(activityId, eventId);
	}
	
	public static void updateActivity(Activity updateActivity, String eventId) throws DataNotFoundException{
		ActivityDatastoreImpl.updateActivity(updateActivity, eventId);
	}
}
