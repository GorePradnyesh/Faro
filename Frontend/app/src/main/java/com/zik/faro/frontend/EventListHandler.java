package com.zik.faro.frontend;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventListHandler {

    private static final int MIN_EVENTS_PAGE_SIZE = 10;
    private static final int MAX_TOTAL_EVENTS_IN_CACHE = 500;


    public EventAdapter acceptedEventAdapter;
    public EventAdapter notAcceptedEventAdapter;


    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of events. Have a Max limit on number of events we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_EVENTS_IN_CACHE to set the max events we will cache


    /*
    * Map of events needed to access events downloaded from the server in O(1) time. The Key to the
    * Map is the eventID String which returns the Event as the value
    */
    private Map<String, Event> eventMap = new ConcurrentHashMap<>();

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

    private EventListHandler(){}

    private String getEventID(){
        String eventIDStr = Integer.toString(this.tempEventID);
        this.tempEventID++;
        return eventIDStr;
    }

    /*
    * Based on the start Date add new event to the list and Map only if it lies between the first
    * and last event retrieved in the list from the server. If it does not lie in between then
    * simply update the server only.
    * But the event is always added to the Map so that it can be accessed on the Event Landing Page
    * and any updates can be sent to the server. If the Event is not added to the list because of
    * the above reason then once we return back from the EventLanding Page we remove it from the
    * Map.
    */
    public ErrorCodes addNewEvent(Event event) {
        //TODO: send update to server and if successful then add event to List and Map below and
        // update the eventID in the Event.
        String eventID = getEventID();

        if(eventID != null) {
            event.setEventId(eventID);
            conditionallyAddNewEventToList(event);
            this.eventMap.put(eventID, event);
            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;
    }

    private void conditionallyAddNewEventToList(Event event) {
        Event tempEvent;
        Calendar tempCalendar;
        Calendar eventCalendar;
        int index;

        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(event);

        assert (eventAdapter != null);
        int lastEventIndex = eventAdapter.list.size() - 1;
        for (index = lastEventIndex; index >= 0; index--) {
            tempEvent = eventAdapter.list.get(index);
            tempCalendar = tempEvent.getStartDateCalendar();
            eventCalendar = event.getStartDateCalendar();

            //Break if new event occurs after temp event
            if (eventCalendar.after(tempCalendar)) {
                break;
            }
        }
        //Insert new event in the index after temp event index
        int newEventIndex = ++index;

        /* We insert the event in the list only if one of the following conditions are met
        * 1. Combined list size is less than the MIN_EVENTS_PAGE_SIZE. Which means that the number
        * of events in the lists is less than what the server can send at one time.
        * 2. newEventIndex lies between the first and the last event in the list.
        */
        if (getCombinedListSize() < MIN_EVENTS_PAGE_SIZE ||
                (newEventIndex < eventAdapter.list.size() &&
                        newEventIndex > 0)) {
            eventAdapter.insert(event, newEventIndex);
            eventAdapter.notifyDataSetChanged();
        }
    }

    private int getCombinedListSize(){
        return acceptedEventAdapter.list.size()+ notAcceptedEventAdapter.list.size();
    }
    private EventAdapter getEventAdapter(Event event){
        switch(event.getEventStatus()){
            case ACCEPTED:
                return acceptedEventAdapter;
            case NOTRESPONDED:
            case MAYBE:
                return notAcceptedEventAdapter;
            default:
                //TODO: How to catch this condition? This should never occur?
                return null;
        }
    }

    /*
    * Funtion to check the sync between Map and List
    */
    private boolean isMapAndListInSync(){
        return (this.eventMap.size() == getCombinedListSize());
    }

    /*
    * For the case when event is part of Map but not inserted to the List, once we return to
    * AppLandingPage from EventLandingPage, i.e. we are completely done with that event we need to
    * remove the event from the Map also to maintain sync between the Map and List.
    * Event is present in the list if
    * 1. event startDate is smaller than or equal to the last event startDate
    * 2. event startDate is greater than
    */
    public void deleteEventFromMapIfNotInList(Event E){
        Calendar eventCalendar;
        Calendar lastEventInListCalendar;
        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(E);
        int lastEventIndex = eventAdapter.list.size() - 1;
        Event lastEventInList = eventAdapter.list.get(lastEventIndex);

        eventCalendar = E.getStartDateCalendar();
        lastEventInListCalendar = lastEventInList.getStartDateCalendar();

        //TODO (Code Review) add condition to not add if it lies before the first or 0th event.
        //Cause in that case if newEventIndex is 0 then we shouldnt add it.
        if (eventCalendar.compareTo(lastEventInListCalendar) == 1){
            this.eventMap.remove(E.getEventId());
        }

        //TODO Check for Map and List sync here. And somehow catch this error
        //TODO Put assert to crash just for debugging. Remove before production.
        boolean issync = isMapAndListInSync();
    }

    public Event getEventFromMap(String eventID){
        return this.eventMap.get(eventID);
    }

    public void removeEventForEditing(Event E){
        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(E);
        eventAdapter.list.remove(E);
        eventAdapter.notifyDataSetChanged();
        eventMap.remove(E.getEventId());
    }

    public void changeEventStatusToYes(Event event){
        removeEventForEditing(event);
        event.setEventStatus(EventStatus.ACCEPTED);
        addNewEvent(event);
    }

    public void changeEventStatusToMaybe(Event event){
        removeEventForEditing(event);
        event.setEventStatus(EventStatus.MAYBE);
        addNewEvent(event);
    }
}
