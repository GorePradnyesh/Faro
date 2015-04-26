package com.zik.faro.frontend;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by nakulshah on 1/8/15.
 */
public class Event{

    private String eventId;                 //Get this from Server
    private String eventName;

    //TODO Seperate the event status and the assignment completion status and represent them in 2
    //different enums
    private EventStatus eventStatus;        //Status of the event for the user
    private EventControlFlag controlFlag;
    //TODO: Dint make this part of class. When displaying just check the status and represent the
    //appropriate image.
    private int imgResource;

    //TODO: Change Server side code from Date Offset to Calendar
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    //TODO change eventCreator boolean to be set based on a check to compare userID to eventCreatorID.
    //eventCreator boolean is not stored or updated to the server.
    private boolean eventCreator;

    /*Whenever the Event constructor is called we can safely assume the following, since it will be
    * only called from Create New Event Page.
    * 1. Caller is the eventCreator since called only from the Create Event Page
    * 2. Status of event is ACCEPTEDANDCOMPLETE
    */
    //TODO Do not call setters in a constructor.
    public Event(String eventName,
                 Calendar startDateCalendar,
                 Calendar endDateCalendar) {
        setEventName(eventName);
        //TODO: (Code Review) Do not delete this. First time when the user opens the event landing
        //page for an event then compare the userid and the creator id and set this boolean.
        setEventCreator(true);
        setStartDateCalendar(startDateCalendar);
        setEndDateCalendar(endDateCalendar);
        setEventStatusProperties(EventStatus.ACCEPTED);
    }

    //TODO (Code Review) Create and overloaded constructor for the time when the Server sends me
    //events. In that case, the constructor will have all the feilds as params.

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

    public void setImgResource(int imgResource) {
        this.imgResource = imgResource;
    }

    public int getImgResource() {
        return imgResource;
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

    void setEventStatusProperties(EventStatus eventStatus){
        setEventStatus(eventStatus);
        switch (eventStatus) {
            case ACCEPTED:
                setImgResource(R.drawable.green);
                break;
            case MAYBE:
                setImgResource(R.drawable.yellow);
                break;
            case NOTRESPONDED:
                setImgResource(R.drawable.red);
        }
    }

}

