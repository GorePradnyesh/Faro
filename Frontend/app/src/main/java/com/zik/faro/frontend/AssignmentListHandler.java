package com.zik.faro.frontend;

import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
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
    * Map is the assignmentID String which returns the Assignment as the value
    */
    private Map<String, AssignmentParentInfo> assignmentMap = new ConcurrentHashMap<>();

    public Assignment getAssignmentCloneFromMap(String assignmentID){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentID);
        Assignment assignment = assignmentParentInfo.getAssignment();
        Gson gson = new Gson();
        String json = gson.toJson(assignment);
        Assignment cloneAssignment = gson.fromJson(json, Assignment.class);
        return cloneAssignment;
    }

    public Assignment getOriginalAssignmentFromMap(String assignmentID){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentID);
        Assignment assignment = assignmentParentInfo.getAssignment();
        return assignment;
    }

    public AssignmentAdapter getAssignmentAdapter (String eventID, Context context) {
        AssignmentAdapter assignmentAdapter = assignmentAdapterMap.get(eventID);
        if (assignmentAdapter == null){
            assignmentAdapter = new AssignmentAdapter(context, R.layout.event_row_style);
            assignmentAdapterMap.put(eventID, assignmentAdapter);
        }
        return assignmentAdapter;
    }

    public void addAssignmentToListAndMap(String eventID, Assignment assignment, String activityID, Context context) {
        removeAssignmentFromListAndMap(eventID, assignment.getId(), context);

        AssignmentParentInfo assignmentParentInfo = new AssignmentParentInfo(assignment, activityID);
        addAssignmentToList(eventID, assignmentParentInfo, context);
        assignmentMap.put(assignment.getId(), assignmentParentInfo);
    }

    public void addAssignmentToList(String eventID, AssignmentParentInfo assignmentParentInfo, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventID, context);
        assignmentAdapter.insert(assignmentParentInfo, 0);
        assignmentAdapter.notifyDataSetChanged();
    }

    public void removeAssignmentFromList(String eventID, String assignmentID, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventID, context);
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentAdapter.list.get(i);
            if (assignmentParentInfo.getAssignment().getId().equals(assignmentID)){
                assignmentAdapter.list.remove(assignmentParentInfo);
            }
        }
    }

    public void removeAssignmentFromListAndMap (String eventID, String assignmentID, Context context){
        removeAssignmentFromList(eventID, assignmentID, context);
        assignmentMap.remove(assignmentID);
    }

    public String getActivityIDForAssignmentID(String assignmentID){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentID);
        return assignmentParentInfo.getActivityID();
    }

    public void clearEverything() {
        if (assignmentAdapterMap != null)
            assignmentAdapterMap.clear();
        if (assignmentMap != null)
            assignmentMap.clear();
    }

    /*public void clearAssignmentListAndMap(String eventID, Context context){
        AssignmentAdapter assignmentAdapter = getAssignmentAdapter(eventID, context);
        if (assignmentAdapter != null){
            assignmentAdapter.list.clear();
        }
        if (assignmentMap != null){
            assignmentMap.clear();
        }
    }*/
}
