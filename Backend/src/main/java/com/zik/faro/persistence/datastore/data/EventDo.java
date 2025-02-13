package com.zik.faro.persistence.datastore.data;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.expense.ExpenseGroup;

@Entity
@XmlRootElement
public class EventDo extends BaseEntityDo{
    
	private String eventName;         
    @Serialize private Calendar startDate;     
    @Serialize private Calendar endDate;
    private boolean controlFlag;      
    private ExpenseGroup expenseGroup;
    private Location location;
    private ObjectStatus status;
    private Assignment assignment;
    private String eventDescription;
    private String eventCreatorId;
    
    public EventDo(final String eventName, final Calendar startDate, final Calendar endDate,
                 final boolean controlFlag, final ExpenseGroup expenseGroup, final Location location) {
    	super(UUID.randomUUID().toString(),1L);
    	this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = ObjectStatus.OPEN;
    }

    public EventDo(final String eventName){
        super(UUID.randomUUID().toString(),0L);
    	this.eventName = eventName;
        this.status = ObjectStatus.OPEN;
    }

    public EventDo(){
       super();
    }

    @XmlElement
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

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	public String getEventCreatorId() {
		return eventCreatorId;
	}

	public void setEventCreatorId(String eventCreatorId) {
		this.eventCreatorId = eventCreatorId;
	}
	
}
