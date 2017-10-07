package com.zik.faro.frontend.handlers;

import android.content.Context;

import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.BaseObjectHandler;
import com.zik.faro.frontend.ui.EventTabType;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.ui.adapters.EventRecyclerViewAdapter;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EventListHandler extends BaseObjectHandler<Event> {

    public static final int MAX_EVENTS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_EVENTS_IN_CACHE = 500;
    private EventRecyclerViewAdapter upcomingEventsAdapter;
    private EventRecyclerViewAdapter notRespondedEventsAdapter;
    private EventRecyclerViewAdapter pastEventsAdapter;


    //private List<Event> eventList = new LinkedList<>();
    private boolean receivedEvents = false;

    /*
     *This is a Singleton class
     */
    private static EventListHandler eventListHandler = null;

    public static EventListHandler getInstance(Context context){
        if (eventListHandler != null){
            return eventListHandler;
        }
        synchronized (EventListHandler.class)
        {
            if(eventListHandler == null) {
                eventListHandler = new EventListHandler(context);
            }

            return eventListHandler;
        }
    }

    private EventListHandler(Context context){
        upcomingEventsAdapter = new EventRecyclerViewAdapter(context);
        notRespondedEventsAdapter = new EventRecyclerViewAdapter(context);
        pastEventsAdapter = new EventRecyclerViewAdapter(context);
    }

    /*
    * Map of events needed to access events downloaded from the server in O(1) time. The Key to the
    * Map is the eventId String which returns the Event as the value
    */
    private Map<String, EventInviteStatusWrapper> eventMap = new ConcurrentHashMap<>();

    public FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private static String TAG = "EventListHandler";

    public boolean isReceivedEvents() {
        return receivedEvents;
    }

    public void setReceivedEvents(boolean receivedEvents) {
        this.receivedEvents = receivedEvents;
    }

    public void clearListAndMap(){
        if (upcomingEventsAdapter != null) {
            upcomingEventsAdapter.clear();
            upcomingEventsAdapter.notifyDataSetChanged();
        }

        if (notRespondedEventsAdapter != null) {
            notRespondedEventsAdapter.clear();
            notRespondedEventsAdapter.notifyDataSetChanged();
        }

        if (pastEventsAdapter != null) {
            pastEventsAdapter.clear();
            pastEventsAdapter.notifyDataSetChanged();
        }

        if (eventMap != null) {
            eventMap.clear();
        }
    }

    public void addDownloadedEventsToListAndMap(List<EventInviteStatusWrapper> eventInviteStatusWrappers){
        clearListAndMap();
        Calendar currentCalendar = Calendar.getInstance();
        List<Event> upcomingEventsList = new LinkedList<>();
        List<Event> notRespondedEventsList = new LinkedList<>();
        List<Event> pastEventsList = new LinkedList<>();

        for (EventInviteStatusWrapper eventInviteStatusWrapper : eventInviteStatusWrappers) {
            Event event = eventInviteStatusWrapper.getEvent();
            EventInviteStatus eventInviteStatus = eventInviteStatusWrapper.getInviteStatus();

            if (event.getEndDate().before(currentCalendar)) {
                pastEventsList.add(event);
            } else {
                if (eventInviteStatus.equals(EventInviteStatus.ACCEPTED) ||
                        eventInviteStatus.equals(EventInviteStatus.MAYBE)) {
                    upcomingEventsList.add(event);
                } else if (eventInviteStatus.equals(EventInviteStatus.INVITED) ||
                        eventInviteStatus.equals(EventInviteStatus.DECLINED)) {
                    notRespondedEventsList.add(event);
                }
            }
            eventMap.put(event.getId(), eventInviteStatusWrapper);
        }

        upcomingEventsAdapter.populateAdapterWithEventList(upcomingEventsList,
                EventTabType.UPCOMING);

        notRespondedEventsAdapter.populateAdapterWithEventList(notRespondedEventsList,
                EventTabType.NOT_RESPONDED);

        pastEventsAdapter.populateAdapterWithEventList(pastEventsList,
                EventTabType.PAST);
    }

    public void addEventToListAndMap(Event receivedEvent, EventInviteStatus eventInviteStatus) {
        /*
         * If the received event is already present in the local database, then we need to delete that and
         * update it with the newly received Event.
         */
        removeEventFromListAndMap(receivedEvent.getId());


        Calendar currentCalendar = Calendar.getInstance();

        if (receivedEvent.getEndDate().before(currentCalendar)) {
            pastEventsAdapter.populateAdapterWithEvent(receivedEvent, EventTabType.PAST);

        } else {
            if (eventInviteStatus.equals(EventInviteStatus.ACCEPTED) ||
                    eventInviteStatus.equals(EventInviteStatus.MAYBE)) {
                upcomingEventsAdapter.populateAdapterWithEvent(receivedEvent, EventTabType.UPCOMING);
            } else {
                notRespondedEventsAdapter.populateAdapterWithEvent(receivedEvent, EventTabType.NOT_RESPONDED);
            }
        }
        EventInviteStatusWrapper eventInviteStatusWrapper = new EventInviteStatusWrapper(receivedEvent, eventInviteStatus);
        eventMap.put(receivedEvent.getId(), eventInviteStatusWrapper);
    }

    public EventRecyclerViewAdapter getEventAdapter(EventTabType eventTabType) {
        if (eventTabType.equals(EventTabType.PAST)) {
            return pastEventsAdapter;
        } else if (eventTabType.equals(EventTabType.UPCOMING)) {
            return upcomingEventsAdapter;
        } else if (eventTabType.equals(EventTabType.NOT_RESPONDED)) {
            return notRespondedEventsAdapter;
        }
        return null;
    }

    /*
     * TODO: Check if the below can be optimized.
     * Below we need to traverse all the lists in the all 3 adapters. This needs to be done to cover
     * the following corner case. Event to be deleted is in the upcoming List and has end time 2:59.
     * By the time the actual delete is done and this function is called, it is 3:00. Due to this,
     * we will try to check for the event in the pastEvents List where it wont be found and hence
     * the deleted event will keep showing the upcoming list.
     */

    private void removeEventFromList(String eventId){
        if (upcomingEventsAdapter.removeEventIfFound(eventId)) {
            return;
        }
        if (notRespondedEventsAdapter.removeEventIfFound(eventId)) {
            return;
        }
        pastEventsAdapter.removeEventIfFound(eventId);
    }

    public void removeEventFromListAndMap(String eventId){
        removeEventFromList(eventId);
        eventMap.remove(eventId);
    }

    /*
        * Function to check the sync between Map and List
        */
    private boolean isMapAndListInSync(){
        return (eventMap.size() == upcomingEventsAdapter.getItemCount() +
                notRespondedEventsAdapter.getItemCount() + pastEventsAdapter.getItemCount());
    }

    /*
     * For the case when event is part of Map but not inserted to the List, once we return to
     * AppLandingPage from EventLandingPage, i.e. we are completely done with that event we need to
     * remove the event from the Map also to maintain sync between the Map and List.
     * New event is present in the list if
     * 1. List size is less than the MAX_EVENTS_PAGE_SIZE. Which means that the number
     * of events in the lists is less than what the server can send at one time.
     * 2. new Event lies between the first and the last event in the list.
     */
    public void deleteEventFromMapIfNotInList(Event event){

        if (upcomingEventsAdapter.findEvent(event.getId())) {
            return;
        }
        if (notRespondedEventsAdapter.findEvent(event.getId())) {
            return;
        }
        if (pastEventsAdapter.findEvent(event.getId())) {
            return;
        }

        eventMap.remove(event.getId());

        //TODO Check for Map and List sync here. And somehow catch this error
        //TODO Put assert to crash just for debugging. Remove before production.
        boolean issync = isMapAndListInSync();
    }

    @Override
    public Event getOriginalObject(String eventId) throws FaroObjectNotFoundException{
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventId);
        if (eventInviteStatusWrapper == null || eventInviteStatusWrapper.getEvent() == null) {
            throw new FaroObjectNotFoundException
                    (MessageFormat.format("Event with id {0} not found in cache", eventId));
        }

        return eventInviteStatusWrapper.getEvent();
    }

    public EventInviteStatus getUserEventStatus(String eventId) {
        EventInviteStatusWrapper eventInviteStatusWrapper = eventMap.get(eventId);
        if (eventInviteStatusWrapper == null)
            return null;
        return eventInviteStatusWrapper.getInviteStatus();
    }


    @Override
    public Class<Event> getType() {
        return Event.class;
    }


    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of events. Have a Max limit on number of events we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_EVENTS_IN_CACHE to set the max events we will cache

    private void conditionallyAddEventToList(Event event, EventInviteStatus eventInviteStatus) {
        /*Calendar currentCalendar = Calendar.getInstance();


        //Collections.sort(list, new EventStartDateComparator());
        eventCalendar = event.getStartDate();

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
        int newEventIndex = ++index;*/

        /* We insert the event in the list only if one of the following conditions are met
        * 1. Combined list size is less than the MAX_EVENTS_PAGE_SIZE. Which means that the number
        * of events in the lists is less than what the server can send at one time.
        * 2. newEventIndex lies between the first and the last event in the list.
        */
        /*if (getCombinedListSize() < MAX_EVENTS_PAGE_SIZE ||
                (newEventIndex < eventAdapter.list.size() &&
                        newEventIndex > 0)) {
            eventAdapter.insert(event, newEventIndex);
            //TODO: pull this out from here since we should not do it for each event, in case when
            // we are fetching multiple events from the server. Do it after all events are fetched.
            // Since notifyDataSetChanged is a very expensive operation.
            eventAdapter.notifyDataSetChanged();
        }*/
    }
}
