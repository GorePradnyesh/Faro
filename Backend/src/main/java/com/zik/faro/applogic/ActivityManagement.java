package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;

import com.zik.faro.api.bean.Activity;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.ActivityDo;
import com.zik.faro.persistence.datastore.ActivityDatastoreImpl;

public class ActivityManagement {
	public static Activity createActivity(final Activity activity){
		ActivityDo activityDo = new ActivityDo(activity.getEventId(), 
				activity.getName(), activity.getDescription(), activity.getLocation(), 
				activity.getDate(), activity.getAssignment());
		ActivityDatastoreImpl.storeActivity(activityDo);
		return ConversionUtils.fromDo(activityDo);
	}
	
	public static List<Activity> getActivities(String eventId){
		List<ActivityDo> activityDoList = ActivityDatastoreImpl.loadActivitiesByEventId(eventId);
		List<Activity>  activities = null;
		if(activityDoList != null && activityDoList.size() > 0){
			activities = new ArrayList<Activity>(activityDoList.size());
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
	
	public static Activity updateActivity(Activity updateActivity, String eventId) throws DataNotFoundException{
		ActivityDo updateActivityDo = ConversionUtils.toDo(updateActivity);
		return ConversionUtils.fromDo(ActivityDatastoreImpl.updateActivity(updateActivityDo, eventId));
	}
}
