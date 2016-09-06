package com.zik.faro.frontend.data;

import com.zik.faro.data.Item;

public class ItemParentInfo {

    private Item item;
    private String eventID;
    private String activityID;
    private String assignmentID;

    public ItemParentInfo(Item item, String eventID, String activityID, String assignmentID) {
        this.item = item;
        this.eventID = eventID;
        this.activityID = activityID;
        this.assignmentID = assignmentID;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getAssignmentID() {
        return assignmentID;
    }

    public void setAssignmentID(String assignmentID) {
        this.assignmentID = assignmentID;
    }
}
