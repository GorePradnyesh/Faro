package com.zik.faro.data;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.expense.ExpenseGroup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@Entity
@XmlRootElement
public class Event {
    @Id @Index
    private String eventId;           // TODO: make this of type ID
    private String eventName;         // TODO: Indexing this wont help. We need to add this to "Full Text Search"
    private DateOffset startDate;           // TODO: Move this to a junction table ?
    private DateOffset endDate;
    @Index
    private boolean controlFlag;      // TODO: Move this to Objectstatus
    private ExpenseGroup expenseGroup;
    private Location location;
    private ObjectStatus status;

    public Event(final String eventName, final DateOffset startDate, final DateOffset endDate,
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

    public Event(final String eventName){
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.status = ObjectStatus.OPEN;
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
        this.status = ObjectStatus.CLOSED;
    }

    @XmlElement
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
