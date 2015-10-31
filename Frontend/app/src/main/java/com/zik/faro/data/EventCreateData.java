package com.zik.faro.data;

import java.util.Calendar;

public class EventCreateData {

    private String          eventName;
    private Calendar        startDate;
    private Calendar        endDate;
    private Location        location;
    private ExpenseGroup    expenseGroup;

    
    
    public EventCreateData(String eventName, Calendar startDate, Calendar endDate, Location location, ExpenseGroup expenseGroup) {
        if(eventName == null || eventName.isEmpty()
                || startDate == null ){
            throw new RuntimeException("Mandatory event name and start date missing");
        }
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.expenseGroup = expenseGroup;
    }

    private EventCreateData() {

    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ExpenseGroup getExpenseGroup() {
        return expenseGroup;
    }

    public void setExpenseGroup(ExpenseGroup expenseGroup) {
        this.expenseGroup = expenseGroup;
    }
}