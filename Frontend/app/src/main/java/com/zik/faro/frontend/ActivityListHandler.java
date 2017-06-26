package com.zik.faro.frontend;


import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.Activity;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityListHandler {
    /*
     * This is a Singleton class
     */
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

    public static final int MAX_ACTIVITIES_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_ACTIVITIES_IN_CACHE = 500;
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    private Map <String, ActivityAdapter>activityAdapterMap = new ConcurrentHashMap<>();

    /*
    * Map of activities needed to access activities downloaded from the server in O(1) time. The Key to the
    * Map is the activityID String which returns the Activity as the value
    */
    private Map<String, Activity> activityMap = new ConcurrentHashMap<>();


    public Activity getActivityCloneFromMap(String activityID){
        Activity activity = activityMap.get(activityID);
        Gson gson = new Gson();
        String json = gson.toJson(activity);
        Activity cloneActivity = gson.fromJson(json, Activity.class);
        return cloneActivity;
    }

    public Activity getOriginalActivityFromMap (String activityID){
        Activity activity = activityMap.get(activityID);
        return activity;
    }


    public ActivityAdapter getActivityAdapter(String eventID, Context context){
        ActivityAdapter activityAdapter = activityAdapterMap.get(eventID);
        if (activityAdapter == null){
            activityAdapter = new ActivityAdapter(context, R.layout.activity_row_style);
            activityAdapterMap.put(eventID, activityAdapter);
        }
        return activityAdapter;
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
    void addActivityToListAndMap(String eventID, Activity activity, Context context) {
        removeActivityFromListAndMap(eventID, activity.getId(), context);
        conditionallyAddActivityToList(eventID, activity, context);
        activityMap.put(activity.getId(), activity);
        assignmentListHandler.addAssignmentToListAndMap(eventID, activity.getAssignment(), activity.getId(), context);
    }

    private void conditionallyAddActivityToList(String eventID, Activity activity, Context context) {
        Activity tempActivity;
        Calendar tempCalendar;
        Calendar activityCalendar;
        int index;

        activityCalendar = activity.getStartDate();

        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
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

    public void addDownloadedActivitiesToListAndMap(String eventID, List <Activity> activityList, Context context){
        for (int i = 0; i < activityList.size(); i++){
            Activity activity = activityList.get(i);
            addActivityToListAndMap(eventID, activity, context);
        }
        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
        activityAdapter.notifyDataSetChanged();
    }

    public int getActivityListSize(String eventID, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
        return activityAdapter.list.size();
    }

    public void removeActivityFromList(String activityID, List<Activity> activityList){
        for (int i = 0; i < activityList.size(); i++){
            Activity activity = activityList.get(i);
            if (activityID.equals(activity.getId())){
                activityList.remove(activity);
                return;
            }
        }
    }

    public void removeActivityFromListAndMap(String eventID, String activityID, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
        removeActivityFromList(activityID, activityAdapter.list);
        activityAdapter.notifyDataSetChanged();
        Activity activity = activityMap.get(activityID);
        if (activity != null && activity.getAssignment() != null) {
            assignmentListHandler.removeAssignmentFromListAndMap(eventID,
                    activity.getAssignment().getId(),
                    context);
        }
        activityMap.remove(activityID);

    }

    /*public void clearActivityListAndMap(String eventID, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
        if (activityAdapter != null){
            activityAdapter.list.clear();
            activityAdapterMap.remove(eventID);
        }
        if (activityMap != null){
            activityMap.clear();
        }
    }*/

    private void clearActivityAdapterIfEmpty(String eventID, String activityID, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventID, context);
        if (activityAdapter != null){
            if (activityAdapter.list.isEmpty()){
                activityAdapterMap.remove(eventID);
            }
        }
    }

    public void clearEverything() {
        activityAdapterMap.clear();
        activityMap.clear();
    }

    //
    // For the case when activity is part of Map but not inserted to the List, once we return to
    // ActivityListPage from ActivityLandingPage, i.e. we are completely done with that activity we need to
    // remove the activity from the Map also to maintain sync between the Map and List.
    // New activity is present in the list if
    // 1. Combined list size is less than the MAX_ACTIVITIES_PAGE_SIZE. Which means that the number
    // of activities in the lists is less than what the server can send at one time.
    // 2. new Activity lies between the first and the last activity in the list.
    //
    /*public void deleteActivityFromMapIfNotInList(Activity activity){
        Calendar activityCalendar;
        Calendar lastActivityInListCalendar;
        int lastActivityIndex = activityAdapter.list.size() - 1;
        Activity lastActivityInList = activityAdapter.list.get(lastActivityIndex);

        activityCalendar = activity.getStartDate();
        lastActivityInListCalendar = lastActivityInList.getStartDate();

        //TODO (Code Review) add condition to not add if it lies before the first or 0th activity.
        //Cause in that case if newActivityIndex is 0 then we shouldnt add it.
        if (activityCalendar.compareTo(lastActivityInListCalendar) == 1){
            activityMap.remove(activity.getId());
        }

        //TODO Check for Map and List sync here. And somehow catch this error
        //TODO Put assert to crash just for debugging. Remove before production.
        boolean issync = isMapAndListInSync();
    }*/
    //
    // Funtion to check the sync between Map and List
    //
    /*private boolean isMapAndListInSync(){
        return (activityMap.size() == activityAdapter.list.size());
    }
    */
}