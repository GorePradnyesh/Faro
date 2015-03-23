package com.example.nakulshah.FrontEnd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nakulshah on 3/1/15.
 */
public class EventListHandler {

    public static final int MIN_EVENTS_PAGE_SIZE = 10;

    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of events. Have a Max limit on number of events we will cache else will use up a lot of
    // memory and battery.


    /* Map of events needed to access events downloaded from the server in O(1) time. The Key to the
    * Map is the eventID String which returns the Event as the value
    * List of events is needed to maintain a sorted list of events downloaded from the server based
    * on the start dates of the events. Sorting and sorted insert operations cannot be done on Maps.
    * Also the list is used to display the events on the AppLandingPage.
    */
    private Map<String, Event> eventMap = new ConcurrentHashMap<String, Event>();
    public List<Event> eventList = new ArrayList<Event>();

    private static int tempEventID = 0;

    private static EventListHandler eventListHandler = null;
    public static EventListHandler getInstance(){
        if (eventListHandler != null){
            return eventListHandler;
        }
        synchronized (EventListHandler.class)
        {
            if(eventListHandler == null)
                eventListHandler = new EventListHandler();
            return eventListHandler;
        }
    }

    private String getEventID(){
        String eventIDStr = Integer.toString(this.tempEventID);
        this.tempEventID++;
        return eventIDStr;
    }

    /*
    * TODO
    * Based on the start Date add new event to the list and Map only if it lies between the first
    * and last event retrieved in the list from the server. If it does not lie in between then
    * simply update the server only.
    * But the event is always added to the Map so that it can be accessed on the Event Landing Page
    * and any updates can be sent to the server.
    */
    public ErrorCodes addNewEvent(Event E) {
        //TODO: send update to server and if successful then add event to List and Map below and
        // update the eventID in the Event.
        String eventID = getEventID();

        if(eventID != null) {
            E.setEventId(eventID);
            addNewEventToList(E);
            this.eventMap.put(eventID, E);
            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;
    }

    private void addNewEventToList(Event E){
        Event tempEvent = null;
        Calendar tempCalendar = null;
        Calendar eventCalendar = null;
        int i = 0;

        if(this.eventList.size() == 0){
            this.eventList.add(i, E);
            return;
        }

        int lastEventIndex = this.eventList.size() - 1;
        for(i = lastEventIndex; i >= 0 ; i--){
            tempEvent = (Event)this.eventList.get(i);
            tempCalendar = tempEvent.getStartDateCalendar();
            eventCalendar = E.getStartDateCalendar();

            //Break if new event occurs after temp event
            if(eventCalendar.compareTo(tempCalendar) == 1){
                break;
            }
        }
        //Insert new event in the index after temp event index
        int newEventIndex = ++i;

        /* We insert the event in the eventList only if one of the following conditions are met
        * 1. eventList size is less than the MIN_EVENTS_PAGE_SIZE
        * 2. newEventIndex is less than eventList size.
        */
        if (eventList.size() < MIN_EVENTS_PAGE_SIZE ||
                newEventIndex < eventList.size()) {
            this.eventList.add(newEventIndex, E);
        }
        return;
    }

    /*
    * Funtion to check the sync between Map and List
    */
    private boolean isMapAndListInSync(){
        return (this.eventMap.size() == this.eventList.size());
    }

    /*
    * For the case when event is part of Map but not inserted to the List, once we return to
    * AppLandingPage from EventLandingPage, i.e. we are completely done with that event we need to
    * remove the event from the Map also to maintain sync between the Map and List.
    * Event is present in the list if
    * 1. event startDate is smaller than or equal to the last event startDate
    */
    public void deleteEventFromMapIfNotInList(Event E){
        Calendar eventCalendar = null;
        Calendar lastEventInListCalendar = null;
        int lastEventIndex = eventList.size() - 1;
        Event lastEventInList = (Event)this.eventList.get(lastEventIndex);

        eventCalendar = E.getStartDateCalendar();
        lastEventInListCalendar = lastEventInList.getStartDateCalendar();

        if (eventCalendar.compareTo(lastEventInListCalendar) == 1){
            this.eventMap.remove(E.getEventId());
        }

        //TODO Check for Map and List sync here. And somehow catch this error
        boolean issync = isMapAndListInSync();
    }

    public Event getEventFromMap(String key){
        return this.eventMap.get(key);
    }

    public int getEventListSize(){
        return this.eventList.size();
    }

    public Event getEventAtPosition(int position) {
        return this.eventList.get(position);
    }

    /*
    * TODO
    * Write a function to handle inserting a non-inserted event to the list or resorting of an
    * already inserted event in case of startDate updation on EventLandingPage
    */


}
