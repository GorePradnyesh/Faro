package com.zik.faro.data;

import com.zik.faro.data.expense.ExpenseGroup;

import java.util.Calendar;
import java.util.UUID;

public class Event extends BaseEntity {
    public static final String NAME = "eventName";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String DESCRIPTION = "eventDescription";
    public static final String CONTROL_FLAG = "controlFlag";
    public static final String EXPENSE_GROUP = "expenseGroup";
    public static final String LOCATION = "location";

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
    

    // *** Server side constructors ***
    public Event(final String eventName, final Calendar startDate, final Calendar endDate,
                 final String eventDescription, final boolean controlFlag, final ExpenseGroup expenseGroup, 
                 final Location location, final ObjectStatus objectStatus, final Assignment assignment,
                 final String eventCreatorId) {
        super(UUID.randomUUID().toString(), 1L);
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventDescription = eventDescription;
        this.controlFlag = controlFlag;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.status = objectStatus;
        this.assignment = assignment;
        this.eventCreatorId = eventCreatorId;
    }

    public Event(final String eventName){
        this(eventName,null,null,null,false, null,null,null, new Assignment(),null);
    }

    // TODO: 05/07/16: Paddy Kaivan spoke about potential problems when a vanilla 
    // event object is expected and you get one with id and assignment initialized
    // Jaxb another avenue where problems possibly can occur which it seems like is
    // overwriting these values.. If problems occur, clean up the below contructor
    // and create overloaded ones as needed
    public Event(){
        this(null,null,null,null,false, null,null,null, new Assignment(),null);
    }
    
    // *** Client constructor ***
    public Event(final String eventName, final Calendar startDate, final Calendar endDate,
                 final boolean controlFlag, String eventDescription,
                 final ExpenseGroup expenseGroup, final Location location,
                 final String eventCreatorId) {
        
    	this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.controlFlag = controlFlag;
        this.eventDescription = eventDescription;
        this.expenseGroup = expenseGroup;
        this.location = location;
        this.eventCreatorId = eventCreatorId;
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

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }

	public Assignment getAssignment() {
		return assignment;
	}

    // for use only in the ConversionUtils
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

    public String getEventCreatorId() {
        return eventCreatorId;
    }

    public void setEventCreatorId(String eventCreatorId) {
        this.eventCreatorId = eventCreatorId;
    }
}

