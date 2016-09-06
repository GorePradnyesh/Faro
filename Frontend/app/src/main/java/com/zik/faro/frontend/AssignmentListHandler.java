package com.zik.faro.frontend;

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

    public AssignmentAdapter assignmentAdapter;

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();

    /*
    * Map of assignments needed to access assignments downloaded from the server in O(1) time. The Key to the
    * Map is the assignmentID String which returns the Assignment as the value
    */
    private Map<String, AssignmentParentInfo> assignmentMap = new ConcurrentHashMap<>();


    public void removeAssignmentFromList(String assignmentID){
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentAdapter.list.get(i);
            if (assignmentParentInfo.getAssignment().getId().equals(assignmentID)){
                assignmentAdapter.list.remove(assignmentParentInfo);
            }
        }
    }

    public void removeAssignmentFromListAndMap (String assignmentID){
        removeAssignmentFromList(assignmentID);
        assignmentMap.remove(assignmentID);
    }

    public void addAssignmentToList(AssignmentParentInfo assignmentParentInfo){
        assignmentAdapter.insert(assignmentParentInfo, 0);
        assignmentAdapter.notifyDataSetChanged();
    }

    public void addAssignmentToListAndMap(Assignment assignment, String activityID) {
        removeAssignmentFromListAndMap(assignment.getId());

        AssignmentParentInfo assignmentParentInfo = new AssignmentParentInfo(assignment, activityID);
        addAssignmentToList(assignmentParentInfo);
        assignmentMap.put(assignment.getId(), assignmentParentInfo);
    }

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

    public String getActivityIDForAssignmentID(String assignmentID){
        AssignmentParentInfo assignmentParentInfo = assignmentMap.get(assignmentID);
        return assignmentParentInfo.getActivityID();
    }

    public void clearAssignmentListAndMap(){
        if (assignmentAdapter != null){
            assignmentAdapter.list.clear();
        }
        if (assignmentMap != null){
            assignmentMap.clear();
        }
    }
}
