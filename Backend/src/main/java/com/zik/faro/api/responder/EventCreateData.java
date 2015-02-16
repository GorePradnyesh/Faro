package com.zik.faro.api.responder;

import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Location;
import com.zik.faro.data.expense.ExpenseGroup;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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

    private EventCreateData() {
        // too satisfy JAXB
        this.eventName = null;
        this.startDate = null;
        this.endDate = null;
        this.location = null;
        this.expenseGroup = null;
    }
}