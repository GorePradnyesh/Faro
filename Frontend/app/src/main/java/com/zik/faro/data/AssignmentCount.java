package com.zik.faro.data;

/**
 * Created by pgore on 10/11/15.
 */
public class AssignmentCount{
    public String eventId;
    public int assignmentCount;
    AssignmentCount(final String eventId, final int assignmentCount){
        this.eventId = eventId;
        this.assignmentCount = assignmentCount;
    }
    AssignmentCount(){}
}