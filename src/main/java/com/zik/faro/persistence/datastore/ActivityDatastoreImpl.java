package com.zik.faro.persistence.datastore;

import java.util.List;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

public class ActivityDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storeActivity(final Activity activity){
        //TODO: Should we ensure that eventId exists before storing the Activity for that EventID
        DatastoreObjectifyDAL.storeObject(activity);
    }

    public static Activity loadActivityById(final String activityId, final String eventId) throws DataNotFoundException{
        Activity activity
                = DatastoreObjectifyDAL.loadObjectWithParentId(Event.class, eventId, Activity.class, activityId);
        return activity;
    }


    //NOTE: Since the activities contain the INDEXED event id this function is placed in the ActivityDatastoreImpl
    public static List<Activity> loadActivitiesByEventId(final String eventId){
        List<Activity> activityList =
                DatastoreObjectifyDAL.loadObjectsByAncestorRef(Event.class, eventId, Activity.class);
        return activityList;
    }
    
    public static void delelteActivityById(final String activityId, final String eventId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(activityId, Activity.class, eventId, Event.class);
    }
    
	public static void updateActivity(final Activity updateActivity, final String eventId) throws DataNotFoundException{
    	Work w = new Work<TransactionResult>() {
	        public TransactionResult run() {
	        	// Read from datastore
	        	Activity activity = null;
				try {
					activity = loadActivityById(updateActivity.getId(), eventId);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
	        	
	            // Modify.
	            activity.setDate(updateActivity.getDate());
	            activity.setDescription(updateActivity.getDescription());
	            activity.setLocation(updateActivity.getLocation());
	            activity.setAssignment(updateActivity.getAssignment());
	            
	            // Store
	            storeActivity(activity);
	            return TransactionResult.SUCCESS;
	        }
	    };
	    
    	TransactionResult result = DatastoreObjectifyDAL.update(w);
    	if(result.equals(TransactionResult.DATANOTFOUND)){
    		//TODO change enum to send message as well to extract key of entity
    		throw new DataNotFoundException("");
    	}
    }
	
	
    /*
    // Ideally this should not be needed as the assignment is contained within the ID and
    // when an Activity is loaded, the assignment should implicitly get loaded with it .
    public Assignment loadAssignmentByActivityId(final String activityId){;}*/
}
