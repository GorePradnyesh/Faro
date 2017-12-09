package com.zik.faro.frontend.database.entities;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.expense.ExpenseGroup;

import java.util.Calendar;

/**
 * Created by gaurav on 11/11/17.
 */
@Entity
public class Event {
    @PrimaryKey
    @NonNull
    private String id;
    private Long version;
    private String eventName;
    //private Calendar startDate;
    //private Calendar endDate;
    private String eventDescription;
    private boolean controlFlag;
    //private ExpenseGroup expenseGroup;
    //@Embedded
    //private Location location;
    //private ObjectStatus status;

    //@Embedded
    //private Assignment assignment;
    private String eventCreatorId;

    public Event() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /*
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
    }*/

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public boolean isControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(boolean controlFlag) {
        this.controlFlag = controlFlag;
    }

    /*public ExpenseGroup getExpenseGroup() {
        return expenseGroup;
    }

    public void setExpenseGroup(ExpenseGroup expenseGroup) {
        this.expenseGroup = expenseGroup;
    }*/

    /*public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }*/

    /*public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }*/

    /*public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }*/

    public String getEventCreatorId() {
        return eventCreatorId;
    }

    public void setEventCreatorId(String eventCreatorId) {
        this.eventCreatorId = eventCreatorId;
    }

    public com.zik.faro.data.Event toFaroData() {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(this), com.zik.faro.data.Event.class);
    }

    public static Event fromFaroData(com.zik.faro.data.Event event) {
        Gson gson = new Gson();
        Event faroEvent = gson.fromJson(gson.toJson(event), Event.class);
        return faroEvent;
    }
}
