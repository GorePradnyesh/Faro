package com.zik.faro.data;

public class AssignmentCount{
    public String eventId;
    public int assignmentCount;
    AssignmentCount(final String eventId, final int assignmentCount){
        this.eventId = eventId;
        this.assignmentCount = assignmentCount;
    }
    AssignmentCount(){}
}