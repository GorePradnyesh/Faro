package com.zik.faro.persistence.datastore;

import com.zik.faro.data.Activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDatastoreImpl {
    private static final String EVENTID_FIELD_NAME = "eventId";

    public static void storeActivity(final Activity activity){
        //TODO: Should we ensure that eventId exists before storing the Activity for that EventID
        DatastoreObjectifyDAL.storeObject(activity);
    }

    public static Activity loadActivityById(final String activityId, final String eventId){
        Activity activity =
                ObjectifyHelper.loadObjectByIdAndEventIdField(activityId, EVENTID_FIELD_NAME, eventId, Activity.class);
        return activity;
    }


    //NOTE: Since the activities contain the INDEXED event id this function is placed in the ActivityDatastoreImpl
    public static List<Activity> loadActivitiesByEventId(final String eventId){
        List<Activity> activityList =
                ObjectifyHelper.loadObjectsForEventId(EVENTID_FIELD_NAME, eventId, Activity.class);
        return activityList;
    }


    /*
    // Ideally this should not be needed as the assignment is contained within the ID and
    // when an Activity is loaded, the assignment should implicitly get loaded with it .
    public Assignment loadAssignmentByActivityId(final String activityId){;}*/
}
