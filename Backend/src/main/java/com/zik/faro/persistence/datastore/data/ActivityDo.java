package com.zik.faro.persistence.datastore.data;


import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialize;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;


@XmlRootElement
@Entity
public class ActivityDo extends BaseEntityDo{

	@Parent
    private Ref<EventDo> eventId;
    private Assignment assignment;
    private String name;
    private String description;
    private Location location;
    @Serialize private Calendar startDate;
    @Serialize private Calendar endDate;
    
    public ActivityDo(String eventId, String name) {
        this(eventId, name, null, null, null, null, null);
    }

    public ActivityDo() {    // TO Satisfy JaxB
    }

    public ActivityDo(String eventId, String name, String description,
    		Location location, Calendar startDate, Calendar endDate, Assignment assignment) {
        this(UUID.randomUUID().toString(),eventId, name, description, location, startDate, endDate, assignment);
    }
    
    public ActivityDo(String id, String eventId, String name, String description, Location location, 
    		Calendar startDate, Calendar endDate, Assignment assignment){
    	super(id,1L);
        this.eventId = Ref.create(Key.create(EventDo.class, eventId));
        this.name = name;
        this.description = description;
        this.location = location;
        this.assignment = assignment;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
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

    public void setAssignment(final Assignment assignment){
        this.assignment = assignment;
    }

    public Assignment getAssignment(){
        return this.assignment;
    }
    
	public void setEventId(String eventId) {
		this.eventId = Ref.create(Key.create(EventDo.class, eventId));;
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

	public void setEventId(Ref<EventDo> eventId) {
		this.eventId = eventId;
	}
	
}
