package com.zik.faro.frontend.data;

import com.google.gson.Gson;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;

import java.util.ArrayList;
import java.util.List;

public class AssignmentParentInfo {
    Assignment assignment;
    String activityID;

    public AssignmentParentInfo(Assignment assignment, String activityID) {
        this.assignment = assignment;
        this.activityID = activityID;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }
}
