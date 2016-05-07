package com.zik.faro.frontend;


import com.zik.faro.data.Activity;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityListHandler{
    public static final int MAX_ACTIVITIES_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_ACTIVITIES_IN_CACHE = 500;


    public ActivityAdapter activityAdapter;

    /*
    * Map of activities needed to access activities downloaded from the server in O(1) time. The Key to the
    * Map is the activityID String which returns the Activity as the value
    */
    private Map<String, Activity> activityMap = new ConcurrentHashMap<>();

    private static int tempActivityID = 0;

    private static ActivityListHandler activityListHandler = null;
    public static ActivityListHandler getInstance(){
        if (activityListHandler != null){
            return activityListHandler;
        }
        synchronized (ActivityListHandler.class)
        {
            if(activityListHandler == null)
                activityListHandler = new ActivityListHandler();
            return activityListHandler;
        }
    }

    private ActivityListHandler(){}

    private String getActivityID(){
        String activityIDStr = Integer.toString(this.tempActivityID);
        this.tempActivityID++;
        return activityIDStr;
    }

    /*
    * Based on the start Date add new activity to the list and Map only if it lies between the first
    * and last activity retrieved in the list from the server. If it does not lie in between then
    * simply update the server only.
    * But the activity is always added to the Map so that it can be accessed on the Activity Landing Page
    * and any updates can be sent to the server. If the activity is not added to the list because of
    * the above reason then once we return back from the ActivityLanding Page we remove it from the
    * Map.
    */
    public ErrorCodes addNewActivity(Activity activity) {
        //TODO: send new activity update to server
        //TODO  update server and if successful then add activity to List and Map below and
        // update the activityID in the activity.

        String activityID = getActivityID();
        if(activityID != null) {
            activity.setId(activityID);

            addNewActivityToList(activity);
            this.activityMap.put(activityID, activity);

            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;

        /*TestEventCreateCallback createCallback = new TestEventCreateCallback();
        EventCreateData eventCreateData= new EventCreateData(event.getEventName(),
                event.getStartDate(), event.getEndDate(), null, null);
        MainActivity.serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        return ErrorCodes.SUCCESS;*/
    }

    private void addNewActivityToList(Activity activity) {
        Activity tempActivity;
        Calendar tempCalendar;
        Calendar activityCalendar;
        int index;

        activityCalendar = activity.getStartDate();

        int lastActivityIndex = activityAdapter.list.size() - 1;
        for (index = lastActivityIndex; index >= 0; index--) {
            tempActivity = activityAdapter.list.get(index);
            tempCalendar = tempActivity.getStartDate();

            //Break if new activity occurs after temp activity
            if (activityCalendar.after(tempCalendar)) {
                break;
            }
        }
        //Insert new activity in the index after temp activity index
        int newActivityIndex = ++index;

        /* We insert the activity in the list only if one of the following conditions are met
        * 1. Combined list size is less than the MAX_ACTIVITIES_PAGE_SIZE. Which means that the number
        * of activities in the lists is less than what the server can send at one time.
        * 2. newACtivityIndex lies between the first and the last activity in the list.
        */
        if (activityAdapter.list.size() < MAX_ACTIVITIES_PAGE_SIZE ||
                (newActivityIndex < activityAdapter.list.size() &&
                        newActivityIndex > 0)) {
            activityAdapter.insert(activity, newActivityIndex);
            activityAdapter.notifyDataSetChanged();
        }
    }

    /*
    * Funtion to check the sync between Map and List
    */
    private boolean isMapAndListInSync(){
        return (this.activityMap.size() == activityAdapter.list.size());
    }


    /*
    * For the case when activity is part of Map but not inserted to the List, once we return to
    * ActivityListPage from ActivityLandingPage, i.e. we are completely done with that activity we need to
    * remove the activity from the Map also to maintain sync between the Map and List.
    * New activity is present in the list if
    * 1. Combined list size is less than the MAX_ACTIVITIES_PAGE_SIZE. Which means that the number
    * of activities in the lists is less than what the server can send at one time.
    * 2. new Activity lies between the first and the last activity in the list.
    */
    public void deleteActivityFromMapIfNotInList(Activity activity){
        Calendar activityCalendar;
        Calendar lastActivityInListCalendar;
        int lastActivityIndex = activityAdapter.list.size() - 1;
        Activity lastActivityInList = activityAdapter.list.get(lastActivityIndex);

        activityCalendar = activity.getStartDate();
        lastActivityInListCalendar = lastActivityInList.getStartDate();

        //TODO (Code Review) add condition to not add if it lies before the first or 0th activity.
        //Cause in that case if newActivityIndex is 0 then we shouldnt add it.
        if (activityCalendar.compareTo(lastActivityInListCalendar) == 1){
            this.activityMap.remove(activity.getId());
        }

        //TODO Check for Map and List sync here. And somehow catch this error
        //TODO Put assert to crash just for debugging. Remove before production.
        boolean issync = isMapAndListInSync();
    }

    public Activity getActivityFromMap(String activityID){
        return this.activityMap.get(activityID);
    }

    public void removeActivityFromListAndMap(Activity activity){
        //TODO: send update to server and if successful then delete activity from List and Map below

        activityAdapter.list.remove(activity);
        activityAdapter.notifyDataSetChanged();
        activityMap.remove(activity.getId());
    }

    public int getActivityListSize(){
        return activityAdapter.list.size();
    }
}