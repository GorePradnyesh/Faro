package com.zik.faro.persistence.datastore;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Work;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

public class ActivityDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";
    private static Logger logger = LoggerFactory.getLogger(ActivityDatastoreImpl.class);
    public static void storeActivity(final ActivityDo activity){
        //TODO: Ensure that eventId exists before storing the Activity for that EventID
        DatastoreObjectifyDAL.storeObject(activity);
        logger.info("Activity created!");
    }

    public static ActivityDo loadActivityById(final String activityId, final String eventId) throws DataNotFoundException{
        ActivityDo activity
                = DatastoreObjectifyDAL.loadObjectWithParentId(EventDo.class, eventId, ActivityDo.class, activityId);
        logger.info("Activity loaded!");
        return activity;
    }


    //NOTE: Since the activities contain the INDEXED event id this function is placed in the ActivityDatastoreImpl
    public static List<ActivityDo> loadActivitiesByEventId(final String eventId){
        List<ActivityDo> activityList =
                DatastoreObjectifyDAL.loadObjectsByAncestorRef(EventDo.class, eventId, ActivityDo.class);
        logger.info("Total activities fetched:"+(activityList == null ? 0 : activityList.size()));
        return activityList;
    }
    
    public static void deleteActivityById(final String activityId, final String eventId){
    	DatastoreObjectifyDAL.deleteObjectByIdWithParentId(activityId, ActivityDo.class, eventId, EventDo.class);
    	logger.info("Activity deleted!");
    }
    
	public static ActivityDo updateActivity(final ActivityDo updateActivity, final String eventId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	Work w = new Work<TransactionResult<ActivityDo>>() {
	        public TransactionResult<ActivityDo> run() {
	        	// Read from datastore
	        	ActivityDo activity = null;
				try {
					activity = loadActivityById(updateActivity.getId(), eventId);
					logger.info("Activity loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<ActivityDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(updateActivity, activity)){
					return new TransactionResult<ActivityDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+activity.getVersion().toString());
				}
				
	            // Modify.
				if(updateActivity.getStartDate() != null){
					activity.setStartDate(updateActivity.getStartDate());
				}
				if(updateActivity.getEndDate() != null){
					activity.setEndDate(updateActivity.getEndDate());
				}
				if(updateActivity.getDescription() != null && !updateActivity.getDescription().isEmpty()){
					activity.setDescription(updateActivity.getDescription());
				}
				if(updateActivity.getLocation() != null){
					activity.setLocation(updateActivity.getLocation());
				}
				
				if(updateActivity.getName() != null){
					activity.setName(updateActivity.getName());
				}
				BaseDatastoreImpl.versionIncrement(updateActivity, activity);

	            // Store
	            storeActivity(activity);
	            return new TransactionResult<ActivityDo>(activity, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<ActivityDo> result = DatastoreObjectifyDAL.update(w);
    	try {
			DatastoreUtil.processResult(result);
		} catch (UpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	logger.info("Activity updated!");
    	return result.getEntity();
    }
	
	
    /*
    // Ideally this should not be needed as the assignment is contained within the ID and
    // when an Activity is loaded, the assignment should implicitly get loaded with it .
    public Assignment loadAssignmentByActivityId(final String activityId){;}*/
}
