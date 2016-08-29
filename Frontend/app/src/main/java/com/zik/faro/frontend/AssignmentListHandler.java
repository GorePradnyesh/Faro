package com.zik.faro.frontend;

import com.google.gson.Gson;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.List;
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
    private static MyItemListHandler myItemListHandler = MyItemListHandler.getInstance();

    /*
    * Map of assignments needed to access assignments downloaded from the server in O(1) time. The Key to the
    * Map is the assignmentID String which returns the Assignment as the value
    */
    private Map<String, Assignment> assignmentMap = new ConcurrentHashMap<>();

    public void addAssignmentToListAndMap(Assignment assignment) {
        String myUserId = faroUserContext.getEmail();
        removeAssignmentFromListAndMap(assignment.getId());
        addAssignmentToList(assignment);
        assignmentMap.put(assignment.getId(), assignment);
        for (int i = 0; i < assignment.getItems().size(); i++){
            Item item = assignment.getItems().get(i);
            if (item.getAssigneeId().equals(myUserId)){
                myItemListHandler.addMyItemToList(item);
            }
        }

    }

    public void addDownloadedAssignmentsToListAndMap(List<Assignment> assignmentList){
        for (int i = 0; i < assignmentList.size(); i++){
            Assignment assignment = assignmentList.get(i);
            addAssignmentToListAndMap(assignment);
        }
    }

    public void addAssignmentToList(Assignment assignment){
        assignmentAdapter.insert(assignment, 0);
        assignmentAdapter.notifyDataSetChanged();
    }

    public Assignment getAssignmentCloneFromMap(String assignmentID){
        Assignment assignment = assignmentMap.get(assignmentID);
        Gson gson = new Gson();
        String json = gson.toJson(assignment);
        Assignment cloneAssignment = gson.fromJson(json, Assignment.class);
        return cloneAssignment;
    }

    public void removeAssignmentFromList(String assignmentID){
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            Assignment assignment = assignmentAdapter.list.get(i);
            if (assignment.getId().equals(assignmentID)){
                assignmentAdapter.list.remove(assignment);
            }
        }
    }

    public void removeAssignmentFromListAndMap (String assignmentID){
        removeAssignmentFromList(assignmentID);
        assignmentMap.remove(assignmentID);
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
