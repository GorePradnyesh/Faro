package com.zik.faro.frontend.handlers;

import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.Assignment;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.adapters.AssignmentAdapter;
import com.zik.faro.frontend.data.AssignmentParentInfo;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssignmentListHandler {

    /*
     *This is a Singleton class
     */
    private static AssignmentListHandler assignmentListHandler = null;

    public static AssignmentListHandler getInstance(){
        if (assignmentListHandler != null){
            return assignmentListHandler;
        }
        synchronized (AssignmentListHandler.class)
        {
            if(assignmentListHandler == null) {
                assignmentListHandler = new AssignmentListHandler();
            }
            return assignmentListHandler;
        }
    }

    private AssignmentListHandler(){}

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();

    public Map<String, AssignmentAdapter> assignmentAdapterMap = new ConcurrentHashMap<>();

    /*
    * Map of assignments needed to access assignments downloaded from the server in O(1) time. The Key to the
    * Map is the assignmentId String which returns the Assignment as the value
    */
    private Map<String, AssignmentParentInfo> assignmentMap = new ConcurrentHashMap<>();

    public Assignment getAssignmentCloneFromMap(String assignmentId){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentId);
        Assignment assignment = assignmentParentInfo.getAssignment();
        Gson gson = new Gson();
        String json = gson.toJson(assignment);
        Assignment cloneAssignment = gson.fromJson(json, Assignment.class);
        return cloneAssignment;
    }

    public Assignment getOriginalAssignmentFromMap(String assignmentId){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentId);
        if (assignmentParentInfo == null) return null;
        Assignment assignment = assignmentParentInfo.getAssignment();
        if (assignment == null) return null;
        return assignment;
    }

    public AssignmentAdapter getAssignmentAdapter (String eventId, Context context) {
        AssignmentAdapter assignmentAdapter = assignmentAdapterMap.get(eventId);
        if (assignmentAdapter == null){
            // TODO: Why is event row style being passed in the below constructor?
            assignmentAdapter = new AssignmentAdapter(context, R.layout.event_row_style);
            assignmentAdapterMap.put(eventId, assignmentAdapter);
        }
        return assignmentAdapter;
    }

    public void addAssignmentToListAndMap(String eventId, Assignment assignment, String activityId, Context context) {
        removeAssignmentFromListAndMap(eventId, assignment.getId(), context);

        AssignmentParentInfo assignmentParentInfo = new AssignmentParentInfo(assignment, activityId);
        addAssignmentToList(eventId, assignmentParentInfo, context);
        assignmentMap.put(assignment.getId(), assignmentParentInfo);
    }

    public void addAssignmentToList(String eventId, AssignmentParentInfo assignmentParentInfo, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventId, context);
        assignmentAdapter.insert(assignmentParentInfo, 0);
        assignmentAdapter.notifyDataSetChanged();
    }

    public void removeAssignmentFromList(String eventId, String assignmentId, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventId, context);
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentAdapter.list.get(i);
            if (assignmentParentInfo.getAssignment().getId().equals(assignmentId)){
                assignmentAdapter.list.remove(assignmentParentInfo);
            }
        }
    }

    public void removeAssignmentFromListAndMap (String eventId, String assignmentId, Context context){
        removeAssignmentFromList(eventId, assignmentId, context);
        assignmentMap.remove(assignmentId);
    }

    public String getActivityIDForAssignmentID(String assignmentId){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentId);
        return assignmentParentInfo.getActivityID();
    }

    public void clearEverything() {
        if (assignmentAdapterMap != null)
            assignmentAdapterMap.clear();
        if (assignmentMap != null)
            assignmentMap.clear();
    }

    /*public void clearAssignmentListAndMap(String eventId, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventId, context);
        if (assignmentAdapter != null){
            assignmentAdapter.list.clear();
        }
        if (assignmentMap != null){
            assignmentMap.clear();
        }
    }*/
}
