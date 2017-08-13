package com.zik.faro.frontend;


import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.Activity;
import com.zik.faro.data.BaseEntity;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityListHandler extends BaseObjectHandler<Activity>{
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
    * Map is the activityId String which returns the Activity as the value
    */
    private Map<String, Activity> activityMap = new ConcurrentHashMap<>();

    public ActivityAdapter getActivityAdapter(String eventId, Context context){
        ActivityAdapter activityAdapter = activityAdapterMap.get(eventId);
        if (activityAdapter == null){
            activityAdapter = new ActivityAdapter(context, R.layout.activity_row_style);
            activityAdapterMap.put(eventId, activityAdapter);
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
    public void addActivityToListAndMap(String eventId, Activity activity, Context context) {
        removeActivityFromListAndMap(eventId, activity.getId(), context);
        conditionallyAddActivityToList(eventId, activity, context);
        activityMap.put(activity.getId(), activity);
        assignmentListHandler.addAssignmentToListAndMap(eventId, activity.getAssignment(), activity.getId(), context);
    }

    private void conditionallyAddActivityToList(String eventId, Activity activity, Context context) {
        Activity tempActivity;
        Calendar tempCalendar;
        Calendar activityCalendar;
        int index;

        activityCalendar = activity.getStartDate();

        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
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

    public void addDownloadedActivitiesToListAndMap(String eventId, List <Activity> activityList, Context context){
        removeAllActivitiesFromListAndMapForEvent(eventId, context);

        for (Activity activity: activityList){
            addActivityToListAndMap(eventId, activity, context);
        }
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        activityAdapter.notifyDataSetChanged();
    }

    public void removeAllActivitiesFromListAndMapForEvent (String eventId, Context context) {
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        for (Iterator<Activity> iterator = activityAdapter.list.iterator(); iterator.hasNext();) {
            Activity activity = iterator.next();
            if (activity.getAssignment() != null) {
                assignmentListHandler.removeAssignmentFromListAndMap(eventId,
                        activity.getAssignment().getId(),
                        context);
            }
            activityMap.remove(activity.getId());
            iterator.remove();
        }
        activityAdapter.notifyDataSetChanged();
    }

    public int getActivityListSize(String eventId, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        return activityAdapter.list.size();
    }

    public void removeActivityFromList(String activityId, List<Activity> activityList){
        for (Iterator<Activity> iterator = activityList.iterator(); iterator.hasNext();) {
            Activity activity = iterator.next();
            if (activityId.equals(activity.getId())){
                iterator.remove();
                return;
            }
        }
    }

    public void removeActivityFromListAndMap(String eventId, String activityId, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        removeActivityFromList(activityId, activityAdapter.list);
        activityAdapter.notifyDataSetChanged();
        Activity activity = activityMap.get(activityId);
        if (activity != null && activity.getAssignment() != null) {
            assignmentListHandler.removeAssignmentFromListAndMap(eventId,
                    activity.getAssignment().getId(),
                    context);
        }
        activityMap.remove(activityId);

    }

    /*public void clearActivityListAndMap(String eventId, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        if (activityAdapter != null){
            activityAdapter.list.clear();
            activityAdapterMap.remove(eventId);
        }
        if (activityMap != null){
            activityMap.clear();
        }
    }*/

    private void clearActivityAdapterIfEmpty(String eventId, String activityId, Context context){
        ActivityAdapter activityAdapter = getActivityAdapter(eventId, context);
        if (activityAdapter != null){
            if (activityAdapter.list.isEmpty()){
                activityAdapterMap.remove(eventId);
            }
        }
    }

    public void clearEverything() {
        activityAdapterMap.clear();
        activityMap.clear();
    }

    @Override
    public Activity getOriginalObject(String activityId) throws FaroObjectNotFoundException {
        Activity activity = activityMap.get(activityId);
        if (activity == null) {
            throw new FaroObjectNotFoundException
                    (MessageFormat.format("Actvity with id {0} not found in global memory", activityId));
        } else {
            return activity;
        }
    }

    @Override
    public Class getType() {
        return null;
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
