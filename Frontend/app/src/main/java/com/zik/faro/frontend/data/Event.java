package com.zik.faro.frontend.data;

public class Event {
    private String eventId;
    private String eventName;
    private DateOffset startDate;
    private DateOffset endDate;
    private boolean controlFlag;
    private ExpenseGroup expenseGroup;
    private Location location;
    private ObjectStatus status;

    public Event(final String eventName, final DateOffset startDate, final DateOffset endDate,
                 final boolean controlFlag, final ExpenseGroup expenseGroup, final Location location) {
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = ObjectStatus.OPEN;
    }

    public String getEventName() {
        return eventName;
    }

    public DateOffset getStartDate() {
        return startDate;
    }

    public void setStartDate(DateOffset startDate) {
        this.startDate = startDate;
    }

    public DateOffset getEndDate() {
        return endDate;
    }

    public void setEndDate(DateOffset endDate) {
        this.endDate = endDate;
    }

    public boolean isControlFlag() {
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

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }
}
