package com.zik.faro.data;

import java.util.Calendar;

public class Activity extends BaseEntity {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String LOCATION = "location";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";

    private String eventId;
    private Assignment assignment;
    private String name;
    private String description;
    private Location location;
    private Calendar startDate;
    private Calendar endDate;

    // Minimalistic constructor
    public Activity(String eventId, String name) {
        this(eventId, name, null, null, null, null, null);
    }
    
    // TO Satisfy JaxB
    public Activity() {    
    }
    
    // Mainly to be used by client who has all info other than id and version
    public Activity(String eventId, String name, String description,
    		Location location, Calendar startDate, Calendar endDate, Assignment assignment) {
        this(null, 1L, eventId, name, description, location, startDate, endDate, assignment);
    }
    
    // Mostly used on server side for to and for communication
    // Also useful during updates when id is known to client
    public Activity(String id, Long version, String eventId, String name, String description,
    		Location location, Calendar startDate, Calendar endDate, Assignment assignment) {
        super(id,version);
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignment = assignment;
    }

    // Getters and setters

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // for use only in conversionUtils
    public void setAssignment(final Assignment assignment){
        this.assignment = assignment;
    }

    public Assignment getAssignment(){
        return this.assignment;
    }

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setName(String name) {
		this.name = name;
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

}
