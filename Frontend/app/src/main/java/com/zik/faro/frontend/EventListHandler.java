package com.zik.faro.frontend;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;
//import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.data.Activity;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.zik.faro.data.Event;

public class EventListHandler {

    /*
     *This is a Singleton class
     */
    private static EventListHandler eventListHandler = null;

    public static EventListHandler getInstance(){
        if (eventListHandler != null){
            return eventListHandler;
        }
        synchronized (EventListHandler.class)
        {
            if(eventListHandler == null) {
                eventListHandler = new EventListHandler();
            }
            return eventListHandler;
        }
    }

    private EventListHandler(){}


    public static final int MAX_EVENTS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_EVENTS_IN_CACHE = 500;

    public EventAdapter acceptedEventAdapter;
    public EventAdapter notAcceptedEventAdapter;

    /*
    * Map of events needed to access events downloaded from the server in O(1) time. The Key to the
    * Map is the eventID String which returns the Event as the value
    */
    private Map<String, EventInviteStatusWrapper> eventMap = new ConcurrentHashMap<>();

    public static FaroServiceHandler serviceHandler;

    protected static final String baseUrl = "http://10.0.2.2:8080/v1/";
    private static String TAG = "EventListHandler";

    private static final FaroServiceHandler CreateFaroServiceHandler()
    {
        FaroServiceHandler innerServiceHandler = null;
        try {
            innerServiceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        } catch (MalformedURLException e) {
            Log.e(TAG, "failed to obtain servicehandler", e);
        }
        return innerServiceHandler;
    }
    
    public static final void CreateFaroServiceHandler(String baseUrl1)
    {
        FaroServiceHandler innerServiceHandler = null;
        try {
            innerServiceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl1));
        } catch (MalformedURLException e) {
            Log.e(TAG, "failed to obtain servicehandler", e);
        }
        serviceHandler =  innerServiceHandler;
    }

    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of events. Have a Max limit on number of events we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_EVENTS_IN_CACHE to set the max events we will cache



    /*
    * Based on the start Date add new event to the list and Map only if it lies between the first
    * and last event retrieved in the list from the server. If it does not lie in between then
    * simply update the server only.
    * But the event is always added to the Map so that it can be accessed on the Event Landing Page
    * and any updates can be sent to the server. If the Event is not added to the list because of
    * the above reason then once we return back from the EventLanding Page we remove it from the
    * Map.
    */
    public ErrorCodes updateEvent(final EventInviteStatusWrapper eventInviteStatusWrapper) {
        //TODO: send new event update to server

        Event event = eventInviteStatusWrapper.getEvent();
        final EventInviteStatus eventInviteStatus = eventInviteStatusWrapper.getInviteStatus();
        serviceHandler.getEventHandler().createEvent(new BaseFaroRequestCallback<Event>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send event create request");
            }

            @Override
            public void onResponse(Event receivedEvent, HttpError error) {
                if (error == null ) {
                    //Since update to server successful, adding event to List and Map below
                    Log.i(TAG, "Response received Successfully");
                    conditionallyAddEventToList(receivedEvent, eventInviteStatus);
                    EventInviteStatusWrapper eventInviteStatusWrapper = new EventInviteStatusWrapper(receivedEvent, EventInviteStatus.ACCEPTED);
                    eventMap.put(receivedEvent.getEventId(), eventInviteStatusWrapper);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, event);
        return ErrorCodes.SUCCESS;
    }

    public void clearListAndMapOnLogout(){
        if (acceptedEventAdapter != null){
            acceptedEventAdapter.list.clear();
        }
        if (notAcceptedEventAdapter != null) {
            notAcceptedEventAdapter.list.clear();
        }
        if (eventMap != null) {
            eventMap.clear();
        }
    }

    public void addEventToListAndMap(Event receivedEvent, EventInviteStatus eventInviteStatus) {
        /*
         * If the received event is already present in the local database, then we need to delete that and
         * update it with the newly received Event.
         */
        removeEventFromListAndMap(receivedEvent.getEventId());
        conditionallyAddEventToList(receivedEvent, eventInviteStatus);
        EventInviteStatusWrapper eventInviteStatusWrapper = new EventInviteStatusWrapper(receivedEvent, eventInviteStatus);
        eventMap.put(receivedEvent.getEventId(), eventInviteStatusWrapper);
    }

    public void addDownloadedEventsToListAndMap(List<EventInviteStatusWrapper> eventInviteStatusWrappers){
        for (int i = 0; i < eventInviteStatusWrappers.size(); i++) {
            EventInviteStatusWrapper eventInviteStatusWrapper = eventInviteStatusWrappers.get(i);

            //Ideally the server should never send us events with status as DECLINED
            EventInviteStatus eventInviteStatus = eventInviteStatusWrapper.getInviteStatus();
            if (eventInviteStatus.equals(EventInviteStatus.DECLINED)){
                continue;
            }
            Event event = eventInviteStatusWrapper.getEvent();
            addEventToListAndMap(event, eventInviteStatus);
        }
        acceptedEventAdapter.notifyDataSetChanged();
        notAcceptedEventAdapter.notifyDataSetChanged();
    }

    private void conditionallyAddEventToList(Event event, EventInviteStatus eventInviteStatus) {
        Event tempEvent;
        Calendar tempCalendar;
        Calendar eventCalendar;
        int index;

        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(eventInviteStatus);

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
            //TODO: pull this out from here since we should not do it for each event, in case when
            // we are fetching multiple events from the server. Do it after all events are fetched.
            // Since notifyDataSetChanged is a very expensive operation.
            eventAdapter.notifyDataSetChanged();
        }
    }

    private int getCombinedListSize(){
        return acceptedEventAdapter.list.size()+ notAcceptedEventAdapter.list.size();
    }

    private EventAdapter getEventAdapter(EventInviteStatus eventInviteStatus){
        switch (eventInviteStatus) {
            case ACCEPTED:
                return acceptedEventAdapter;
            case INVITED:
            case MAYBE:
                return notAcceptedEventAdapter;
            default:
                //TODO: How to catch this condition? This should never occur?
                return null;
        }
    }

    /*
    * Function to check the sync between Map and List
    */
    private boolean isMapAndListInSync(){
        return (eventMap.size() == getCombinedListSize());
    }

    /*
    * For the case when event is part of Map but not inserted to the List, once we return to
    * AppLandingPage from EventLandingPage, i.e. we are completely done with that event we need to
    * remove the event from the Map also to maintain sync between the Map and List.
    * New event is present in the list if
    * 1. Combined list size is less than the MAX_EVENTS_PAGE_SIZE. Which means that the number
    * of events in the lists is less than what the server can send at one time.
    * 2. new Event lies between the first and the last event in the list.
    */
    public void deleteEventFromMapIfNotInList(Event event){
        Calendar eventCalendar;
        Calendar lastEventInListCalendar;
        EventAdapter eventAdapter;
        EventInviteStatus eventInviteStatus = getUserEventStatus(event.getEventId());
        eventAdapter = getEventAdapter(eventInviteStatus);
        int lastEventIndex = eventAdapter.list.size() - 1;
        Event lastEventInList = eventAdapter.list.get(lastEventIndex);

        eventCalendar = event.getStartDate();
        lastEventInListCalendar = lastEventInList.getStartDate();

        //TODO (Code Review) add condition to not add if it lies before the first or 0th event.
        //Cause in that case if newEventIndex is 0 then we shouldnt add it.
        if (eventCalendar.compareTo(lastEventInListCalendar) == 1){
            eventMap.remove(event.getEventId());
        }

        //TODO Check for Map and List sync here. And somehow catch this error
        //TODO Put assert to crash just for debugging. Remove before production.
        boolean issync = isMapAndListInSync();
    }

    public Event getEventCloneFromMap(String eventID){
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventID);
        Event event = eventInviteStatusWrapper.getEvent();
        Gson gson = new Gson();
        String json = gson.toJson(event);
        Event cloneEvent = gson.fromJson(json, Event.class);
        return cloneEvent;
    }

    public Event getOriginalEventFromMap (String eventID){
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventID);
        Event event = eventInviteStatusWrapper.getEvent();
        return event;
    }

    public EventInviteStatus getUserEventStatus(String eventId) {
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventId);
        return eventInviteStatusWrapper.getInviteStatus();
    }

    public void removeEventFromList(String eventID, List<Event> eventList){
        for (int i = 0; i < eventList.size(); i++){
            Event event = eventList.get(i);
            if (event.getEventId().equals(eventID)){
                eventList.remove(event);
                return;
            }
        }
    }

    public void removeEventFromListAndMap(String eventID){
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventID);
        if (eventInviteStatusWrapper == null){
            return;
        }

        EventInviteStatus eventInviteStatus = getUserEventStatus(eventID);

        EventAdapter eventAdapter;
        eventAdapter = getEventAdapter(eventInviteStatus);
        if (eventAdapter != null) {
            removeEventFromList(eventID, eventAdapter.list);
            eventAdapter.notifyDataSetChanged();
        }
        eventMap.remove(eventID);
    }

    public int getAcceptedEventListSize(){
        return acceptedEventAdapter.list.size();
    }
}