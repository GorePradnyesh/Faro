package com.zik.faro.frontend;


import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Poll;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssignmentListHandler {
    public AssignmentAdapter assignmentAdapter;

    /*
    * Map of assignments needed to access assignments downloaded from the server in O(1) time. The Key to the
    * Map is the assignmentID String which returns the Assignment as the value
    */
    private Map<String, Assignment> assignmentMap = new ConcurrentHashMap<>();

    private static int tempAssignmentID = 0;

    private static AssignmentListHandler assignmentListHandler = null;
    public static AssignmentListHandler getInstance(){
        if (assignmentListHandler != null){
            return assignmentListHandler;
        }
        synchronized (AssignmentListHandler.class)
        {
            if(assignmentListHandler == null)
                assignmentListHandler = new AssignmentListHandler();
            return assignmentListHandler;
        }
    }

    private AssignmentListHandler(){}

    private String getAssignmentID(){
        String assignmentIDStr = Integer.toString(this.tempAssignmentID);
        this.tempAssignmentID++;
        return assignmentIDStr;
    }

    public ErrorCodes addNewAssignment(Assignment assignment) {
        //TODO: send update to server and if successful then add assignment to List and Map below and
        // update the assignmentID in the Assignment.
        String assignmentID = getAssignmentID();

        if(assignmentID != null) {
            assignment.setId(assignmentID);
            assignmentAdapter.insert(assignment, 0);
            assignmentAdapter.notifyDataSetChanged();
            this.assignmentMap.put(assignmentID, assignment);
            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;
    }

    public void deleteAssignment(Assignment assignment){
        //TODO: send update to server and if successful then delete assignment from List and Map below
            assignmentAdapter.list.remove(assignment);
            this.assignmentMap.remove(assignment.getId());
            assignmentAdapter.notifyDataSetChanged();
    }

    public void removeAssignmentForEditing (Assignment assignment){
        assignmentAdapter.list.remove(assignment);
        assignmentAdapter.notifyDataSetChanged();
        assignmentMap.remove(assignment.getId());
    }

    public Assignment getAssignmentFromMap(String assignmentID){
        return this.assignmentMap.get(assignmentID);
    }

}
