package data;

import java.util.Calendar;
import java.util.UUID;

public class Activity {
    private String id;
    private String eventId;
    private Assignment assignment;
    private String name;

    private String description;
    private Location location;
    private Calendar date;

    public Activity(String eventId, String name) {
        this(eventId, name, null, null, null);
    }

    private Activity() {    // TO Satisfy JaxB
        this(null, null);
    }

    public Activity(String eventId, String name, String description, Location location, Calendar date) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
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

    public static class ActivityUpdateData {
        private String description;
        private Calendar date;
        private Location location;

        private ActivityUpdateData(){
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

    }
}