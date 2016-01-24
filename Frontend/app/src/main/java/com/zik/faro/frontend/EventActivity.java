package com.zik.faro.frontend;

import java.util.Calendar;
import java.util.UUID;

public class EventActivity {
    private String id;
    private Assignment assignment;
    private String name;

    private String description;
    private Location location;
    private Calendar startDate;
    private Calendar endDate;

    public EventActivity(String eventId, String name) {
        this(eventId, name, null, null, null, null);
    }

    public EventActivity() {    // TO Satisfy JaxB
    }

    public EventActivity(String eventId, String name, String description,
                         Location location, Calendar startDate, Assignment assignment) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.assignment = assignment;
    }

    public EventActivity(String name, String description,
                         Location location, Calendar startDate, Calendar endDate) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.assignment = assignment;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    /*public String getEventId() {
        return eventId.getKey().getName();
    }*/

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

    public void setAssignment(final Assignment assignment){
        this.assignment = assignment;
    }

    public Assignment getAssignment(){
        return this.assignment;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*public void setEventId(String eventId) {
        this.eventId = Ref.create(Key.create(EventDo.class, eventId));;
    }*/

    public void setName(String name) {
        this.name = name;
    }
}