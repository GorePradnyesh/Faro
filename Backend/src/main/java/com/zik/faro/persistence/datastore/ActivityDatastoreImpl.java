package com.zik.faro.persistence.datastore;

import java.util.Date;
import java.util.List;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.ActivityDo;
import com.zik.faro.data.EventDo;

public class ActivityDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storeActivity(final ActivityDo activity){
        //TODO: Ensure that eventId exists before storing the Activity for that EventID
        DatastoreObjectifyDAL.storeObject(activity);
    }

    public static ActivityDo loadActivityById(final String activityId, final String eventId) throws DataNotFoundException{
        ActivityDo activity
                = DatastoreObjectifyDAL.loadObjectWithParentId(EventDo.class, eventId, ActivityDo.class, activityId);
        return activity;
    }


    //NOTE: Since the activities contain the INDEXED event id this function is placed in the ActivityDatastoreImpl
    public static List<ActivityDo> loadActivitiesByEventId(final String eventId){
        List<ActivityDo> activityList =
                DatastoreObjectifyDAL.loadObjectsByAncestorRef(EventDo.class, eventId, ActivityDo.class);
        return activityList;
    }
    
    public static void delelteActivityById(final String activityId, final String eventId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(activityId, ActivityDo.class, eventId, EventDo.class);
    }
    
	public static ActivityDo updateActivity(final ActivityDo updateActivity, final String eventId) throws DataNotFoundException{
    	Work w = new Work<TransactionResult>() {
	        public TransactionResult run() {
	        	// Read from datastore
	        	ActivityDo activity = null;
				try {
					activity = loadActivityById(updateActivity.getId(), eventId);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
	        	
	            // Modify.
				if(updateActivity.getDate() != null){
					activity.setDate(updateActivity.getDate());
				}
				if(updateActivity.getDescription() != null && !updateActivity.getDescription().isEmpty()){
					activity.setDescription(updateActivity.getDescription());
				}
				if(updateActivity.getLocation() != null){
					activity.setLocation(updateActivity.getLocation());
				}
				if(updateActivity.getAssignment() != null){
					activity.setAssignment(updateActivity.getAssignment());
				}
				
	            // Store
	            storeActivity(activity);
	            return TransactionResult.SUCCESS;
	        }
	    };
	    
    	TransactionResult result = DatastoreObjectifyDAL.update(w);
    	if(result.equals(TransactionResult.DATANOTFOUND)){
    		throw new DataNotFoundException("Activity not found");
    	}
    	return updateActivity;
    }
	
	
    /*
    // Ideally this should not be needed as the assignment is contained within the ID and
    // when an Activity is loaded, the assignment should implicitly get loaded with it .
    public Assignment loadAssignmentByActivityId(final String activityId){;}*/
}
