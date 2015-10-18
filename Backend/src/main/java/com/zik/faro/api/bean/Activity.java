package com.zik.faro.api.bean;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;

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
        this(eventId, name, null, null, null, null);
    }
    
    // TO Satisfy JaxB
    public Activity() {    
    }
    
    // Mainly to be used by client who has all info other than id
    public Activity(String eventId, String name, String description,
    		Location location, Calendar date, Assignment assignment) {
        this(null, eventId, name, description, location, date, assignment);
    }
    
    // Mostly used on server side for to and for communication
    // Also useful during updates when id is known to client
    public Activity(String id, String eventId, String name, String description,
    		Location location, Calendar date, Assignment assignment) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.assignment = assignment;
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
