package com.zik.faro.frontend;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by nakulshah on 1/8/15.
 */
public class Event{

    private String eventId;                 //Get this from Server
    private String eventName;

    private EventStatus eventStatus;        //Status of the event for the user
    private EventControlFlag controlFlag;

    //TODO: Change Server side code from Date Offset to Calendar
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    private String eventDescription;

    /*TODO change eventCreator boolean to be set based on a check to compare userID to eventCreatorID.
    eventCreator boolean is not stored or updated to the server. It should not be part of the event
    This is only to see whether the user gets to see the control flag option on eventEdit Page.
    That can be determined by comparing the eventCreator userID and my own userID.
    */
    private boolean eventCreator;

    public Event(String eventName,
                 Calendar startDateCalendar,
                 Calendar endDateCalendar,
                 EventStatus eventStatus,
                 String eventDescription) {
        this.eventName = eventName;
        this.eventCreator = true;
        this.startDateCalendar = startDateCalendar;
        this.endDateCalendar = endDateCalendar;
        this.eventStatus = eventStatus;
        this.eventDescription = eventDescription;
    }

    //TODO (Code Review) Create and overloaded constructor for the time when the Server sends me
    //events. In that case, the constructor will have all the fields as params.

    //Getters and Setters
    public boolean isEventCreator() {
        return eventCreator;
    }

    public void setEventCreator(boolean eventCreator) {
        this.eventCreator = eventCreator;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    String getEventName(){
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public EventControlFlag getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(EventControlFlag controlFlag) {
        this.controlFlag = controlFlag;
    }

    public Calendar getStartDateCalendar() {
        return startDateCalendar;
    }

    public void setStartDateCalendar(Calendar startDateCalendar) {
        this.startDateCalendar = startDateCalendar;
    }

    public Calendar getEndDateCalendar() {
        return endDateCalendar;
    }

    public void setEndDateCalendar(Calendar endDateCalendar) {
        this.endDateCalendar = endDateCalendar;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
}

