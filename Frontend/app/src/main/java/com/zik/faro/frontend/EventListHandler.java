package com.zik.faro.frontend;

import com.squareup.okhttp.Request;
//import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.user.InviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.zik.faro.data.Event;

public class EventListHandler {

    public static final int MAX_EVENTS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_EVENTS_IN_CACHE = 500;


    public EventAdapter acceptedEventAdapter;
    public EventAdapter notAcceptedEventAdapter;



    //TODO: Temp variable
    private String myUserId = "TestUser";
    //TODO: Temp Map
    private Map<String, EventUser> eventUserMap = new ConcurrentHashMap<>();


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

    public String getMyUserId() {
        return myUserId;
    }

    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    private String getEventID(){
        String eventIDStr = Integer.toString(this.tempEventID);
        this.tempEventID++;
        return eventIDStr;
    }

    final static class TestEventCreateCallback
            implements BaseFaroRequestCallback<com.zik.faro.data.Event> {
        com.zik.faro.data.Event receivedEvent;

        @Override
        public void onFailure(Request request, IOException e) {
            //Handle failure
        }

        @Override
        public void onResponse(com.zik.faro.data.Event event, HttpError error){
            if(error != null){
            }
            // Assert that event == min event
            this.receivedEvent = event;
            String eventID = event.getEventId();
            if(eventID != null) {
                //eventListHandler.conditionallyAddNewEventToList(event);
                //eventListHandler.eventMap.put(eventID, event);
            }

        }
    }

    public ErrorCodes addNewEventUser(String eventId, String userId, EventUser eventUser) {
        String key = eventId + userId;
        eventUserMap.put(key, eventUser);
        return ErrorCodes.SUCCESS;
    }

    public EventUser getEventUser(String eventId, String userId){
        String key = eventId + userId;
        EventUser eventUser = eventUserMap.get(key);
        return eventUser;
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
    //TODO: Second param is temp for testing purpose only. Remove it later!!!
    public ErrorCodes addNewEvent(Event event, InviteStatus inviteStatus) {
        //TODO: send new event update to server
        //TODO  update server and if successful then add event to List and Map below and
        // update the eventID in the Event.

        String eventID = getEventID();
        if(eventID != null) {
            event.setEventId(eventID);

            //Create Event User Relationship
            final EventUser eventUser = new EventUser(event.getEventId(),
                    myUserId,
                    event.getEventCreatorId());
            eventUser.setInviteStatus(inviteStatus);



            ErrorCodes errorCode;
            errorCode = eventListHandler.addNewEventUser(event.getEventId(), myUserId,
                    eventUser);
            if(errorCode == ErrorCodes.SUCCESS) {
                conditionallyAddNewEventToList(event);
                this.eventMap.put(eventID, event);
            }

            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;

        /*TestEventCreateCallback createCallback = new TestEventCreateCallback();
        EventCreateData eventCreateData= new EventCreateData(event.getEventName(),
                event.getStartDate(), event.getEndDate(), null, null);
        MainActivity.serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        return ErrorCodes.SUCCESS;*/



    }

    private void conditionallyAddNewEventToList(Event event) {
        Event tempEvent;
        Calendar tempCalendar;
        Calendar eventCalendar;
        int index;

        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(event);

        eventCalendar = event.getStartDate();
        
        assert (eventAdapter != null);
        int lastEventIndex = eventAdapter.list.size() - 1;
        for (index = lastEventIndex; index >= 0; index--) {
            tempEvent = eventAdapter.list.get(index);
            tempCalendar = tempEvent.getStartDate();


            //Break if new event occurs after temp event
            if (eventCalendar.after(tempCalendar)) {
                break;
            }
        }
        //Insert new event in the index after temp event index
        int newEventIndex = ++index;

        /* We insert the event in the list only if one of the following conditions are met
        * 1. Combined list size is less than the MAX_EVENTS_PAGE_SIZE. Which means that the number
        * of events in the lists is less than what the server can send at one time.
        * 2. newEventIndex lies between the first and the last event in the list.
        */
        if (getCombinedListSize() < MAX_EVENTS_PAGE_SIZE ||
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
        //TODO Change below code to get status from event-User relationship

        String key = event.getEventId()+ myUserId;
        EventUser eventUser = eventUserMap.get(key);
        if(eventUser != null) {
            switch (eventUser.getInviteStatus()) {
                case ACCEPTED:
                    return acceptedEventAdapter;
                case INVITED:
                case MAYBE:
                    return notAcceptedEventAdapter;
                default:
                    //TODO: How to catch this condition? This should never occur?
                    return null;
            }
        }else{
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
    * EventListPage from EventLandingPage, i.e. we are completely done with that event we need to
    * remove the event from the Map also to maintain sync between the Map and List.
    * New event is present in the list if
    * 1. Combined list size is less than the MAX_EVENTS_PAGE_SIZE. Which means that the number
    * of events in the lists is less than what the server can send at one time.
    * 2. new Event lies between the first and the last event in the list.
    */
    public void deleteEventFromMapIfNotInList(Event E){
        Calendar eventCalendar;
        Calendar lastEventInListCalendar;
        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(E);
        int lastEventIndex = eventAdapter.list.size() - 1;
        Event lastEventInList = eventAdapter.list.get(lastEventIndex);

        eventCalendar = E.getStartDate();
        lastEventInListCalendar = lastEventInList.getStartDate();

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

    public void removeEventFromListAndMap(Event E){
        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(E);
        if (eventAdapter != null) {
            eventAdapter.list.remove(E);
            eventAdapter.notifyDataSetChanged();
        }
        eventMap.remove(E.getEventId());
    }

    public void changeEventStatusToYes(Event event){
        removeEventFromListAndMap(event);
        String key = event.getEventId()+ myUserId;
        EventUser eventUser = eventUserMap.get(key);
        eventUser.setInviteStatus(InviteStatus.ACCEPTED);
        addNewEvent(event, eventUser.getInviteStatus());
    }

    public void changeEventStatusToMaybe(Event event){
        removeEventFromListAndMap(event);
        String key = event.getEventId()+ myUserId;
        EventUser eventUser = eventUserMap.get(key);
        eventUser.setInviteStatus(InviteStatus.MAYBE);
        addNewEvent(event, eventUser.getInviteStatus());
    }

    public int getAcceptedEventListSize(){
        return acceptedEventAdapter.list.size();
    }
}
