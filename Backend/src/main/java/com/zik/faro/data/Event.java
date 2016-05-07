package com.zik.faro.data;

import java.util.Calendar;
import java.util.UUID;

import com.zik.faro.data.expense.ExpenseGroup;

public class Event {
    private String eventId;           
    private String eventName;
    private Calendar startDate;     
    private Calendar endDate;
    private String eventDescription;
    private boolean controlFlag;
    private ExpenseGroup expenseGroup;
    private Location location;
    private ObjectStatus status;
    private Assignment assignment;
    private String eventCreatorId;

    public Event(final String eventName, final Calendar startDate, final Calendar endDate,
                 final boolean controlFlag, final ExpenseGroup expenseGroup, final Location location) {
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = ObjectStatus.OPEN;
    }

    public Event(final String eventName, final Calendar startDate, final Calendar endDate,
                 final boolean controlFlag, String eventDescription,
                 final ExpenseGroup expenseGroup, final Location location, final ObjectStatus status,
                 final String eventCreatorId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.eventDescription = eventDescription;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = status;
        this.eventCreatorId = eventCreatorId;
    }

    public Event(final String eventName){
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.status = ObjectStatus.OPEN;
    }

    public Event(){
       
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventName() {
    	return eventName;
    }
    
    public void setEventName(String eventName){
    	this.eventName = eventName;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
    	return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public boolean getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(boolean controlFlag) {
        this.controlFlag = controlFlag;
    }

    public ExpenseGroup getExpenseGroup() {
        return expenseGroup;
    }

    public void setExpenseGroup(ExpenseGroup expenseGroup) {
        this.expenseGroup = expenseGroup;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void markEventClosed(){
        this.status = ObjectStatus.CLOSED;
    }

    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId){
    	this.eventId = eventId;
    }

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }

	public Assignment getAssignment() {
		return assignment;
	}

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	
	public static void main(String[] args) {
		System.out.println(Calendar.getInstance());
	}

    public String getEventCreatorId() {
        return eventCreatorId;
    }

    public void setEventCreatorId(String eventCreatorId) {
        this.eventCreatorId = eventCreatorId;
    }
}

