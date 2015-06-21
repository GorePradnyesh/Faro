package com.zik.faro.data;


import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialize;


@XmlRootElement
@Entity
public class Activity {
    @Id
    private String id;
    @Parent
    private Ref<Event> eventId;
    private Assignment assignment;
    private String name;

    private String description;
    private Location location;
    @Serialize private Calendar date;
    
    public Activity(String eventId, String name) {
        this(eventId, name, null, null, null, null);
    }

    private Activity() {    // TO Satisfy JaxB
    }

    public Activity(String eventId, String name, String description,
    		Location location, Calendar date, Assignment assignment) {
        this.id = UUID.randomUUID().toString();;
        this.eventId = Ref.create(Key.create(Event.class, eventId));
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
        return eventId.getKey().getName();
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
}
