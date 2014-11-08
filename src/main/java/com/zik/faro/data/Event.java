package com.zik.faro.data;

import com.zik.faro.data.expense.ExpenseGroup;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class Event {
    private final String eventId; // TODO: make this of type ID
    private final String eventName;
    private DateOffset startDate;
    private DateOffset endDate;
    private boolean controlFlag;
    private ExpenseGroup expenseGroup;
    private Location location;
    private ItemStatus status;

    public Event(final String eventName, final DateOffset startDate, final DateOffset endDate,
                 final boolean controlFlag, final ExpenseGroup expenseGroup, final Location location) {
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = ItemStatus.OPEN;
    }

    public Event(final String eventName){
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.status = ItemStatus.OPEN;
    }

    private Event(){
        this(null); // To satisfy JAXB
    }

    @XmlElement
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
        this.status = ItemStatus.CLOSE;
    }

    @XmlElement
    public String getEventId() {
        return eventId;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}
