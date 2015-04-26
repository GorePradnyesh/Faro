package com.zik.faro.frontend.data;

public class EventCreateData {
    public final String         eventName;
    public final DateOffset     startDate;
    public final DateOffset     endDate;
    public final Location       location;
    public final ExpenseGroup   expenseGroup;

    public EventCreateData(String eventName, DateOffset startDate, DateOffset endDate, Location location, ExpenseGroup expenseGroup) {
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.expenseGroup = expenseGroup;
    }
}