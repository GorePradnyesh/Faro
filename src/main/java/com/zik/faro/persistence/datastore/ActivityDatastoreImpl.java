package com.zik.faro.persistence.datastore;

import com.zik.faro.data.Activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storeActivity(final Activity activity){
        DatastoreObjectifyDAL.storeObject(activity);
    }

    public static Activity loadActivityById(final String activityId, final String eventId){
        Map<DatastoreOperator, String> filterKeyMap = new HashMap<>();
        filterKeyMap.put(DatastoreOperator.EQ, activityId);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put(EVENTID_FIELD_NAME, eventId);

        List<Activity> activityList =
                DatastoreObjectifyDAL.loadObjectsByFilters(filterKeyMap, filterMap, Activity.class);
        if(activityList.size() > 1){
            throw new RuntimeException("Unexpected condition !! Got multiple Activities for single activity ID");
        }
        if(activityList.size() == 0 ){
            return null; //TODO: better response
        }
        return activityList.get(0);
    }


    //NOTE: Since the activities contain the INDEXED event id this function is placed in the ActivityDatastoreImpl
    public static List<Activity> loadActivitiesByEventId(final String eventId){
        return null;
    }


    /*
    // Ideally this should not be needed as the assignment is contained within the ID and
    // when an Activity is loaded, the assignment should implicitly get loaded with it .
    public Assignment loadAssignmentByActivityId(final String activityId){

    }*/
}
