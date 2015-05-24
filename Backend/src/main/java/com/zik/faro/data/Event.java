package com.zik.faro.data;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.expense.ExpenseGroup;

@Entity
@XmlRootElement
public class Event {
    @Id @Index
    private String eventId;           
    private String eventName;         
    private Calendar startDate;     
    private Calendar endDate;
    private boolean controlFlag;      
    private ExpenseGroup expenseGroup;
    private Location location;
    private ObjectStatus status;
    private Assignment assignment;

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

    public Event(final String eventName){
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.status = ObjectStatus.OPEN;
    }

    private Event(){
       
    }

    @XmlElement
    public String getEventName() {
        return eventName;
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

	public Assignment getAssignment() {
		return assignment;
	}

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	
}
