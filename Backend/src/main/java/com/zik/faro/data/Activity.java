package com.zik.faro.data;

import java.util.Calendar;

public class Activity {
    private String id;
    private String eventId;
    private Assignment assignment;
    private String name;
    private String description;
    private Location location;
    private Calendar date;
    
    // Minimalistic constructor
    public Activity(String eventId, String name) {
        this(eventId, name, null, null, null);
    }
    
    // TO Satisfy JaxB
    public Activity() {    
    }
    
    // Mainly to be used by client who has all info other than id
    public Activity(String eventId, String name, String description,
    		Location location, Calendar date) {
        this(null, eventId, name, description, location, date);
    }
    
    // Mostly used on server side for to and for communication
    // Also useful during updates when id is known to client
    public Activity(String id, String eventId, String name, String description,
    		Location location, Calendar date) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.assignment = new Assignment();
    }

    // Getters and setters

    public String getId() {
    	return id;
    }

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

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    // for use only in conversionUtils
    public void setAssignment(final Assignment assignment){
        this.assignment = assignment;
    }

    public Assignment getAssignment(){
        return this.assignment;
    }
    
    public void setId(String id) {
		this.id = id;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setName(String name) {
		this.name = name;
	}
}
