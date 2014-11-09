package com.zik.faro.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class Activity {
    public final String id;         //TODO: Make into type Id
    public final String eventId;            //TODO: Make into type Id
    public final String name;

    private String description;
    private Location location;
    private DateOffset date;

    public Activity(String eventId, String name) {
        this(eventId, name, null, null, null);
    }

    private Activity() {    // TO Satisfy JaxB
        this(null, null);
    }

    public Activity(String eventId, String name, String description, Location location, DateOffset date) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
    }

    // Getters and setters

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

    public DateOffset getDate() {
        return date;
    }

    public void setDate(DateOffset date) {
        this.date = date;
    }

}
