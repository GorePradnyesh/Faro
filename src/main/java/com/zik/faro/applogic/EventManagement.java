package com.zik.faro.applogic;

import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;

import javax.xml.bind.annotation.XmlRootElement;

//TODO: better name ?
//TODO: Need to add retry logic to the operations
public class EventManagement {

    public static MinEvent createEvent(final String userId, final EventCreateData eventCreateData){
        //TODO: create entry in the Event User table, before creating the event.

        /*Create a new event with the provided data, with the FALSE control flag.*/
        Event newEvent = new Event(eventCreateData.eventName, eventCreateData.startDate,
                eventCreateData.endDate, false, eventCreateData.expenseGroup, eventCreateData.location);
        EventDatastoreImpl.storeEvent(newEvent);
        //TODO: send out notifications t
        return new MinEvent(newEvent.getEventId(), newEvent.getEventName());
    }

    public static Event getEventDetails(final String userId, final String eventId){
        //TODO: Validate that the user has permissions to access eventId, from the EventUser table
        Event event = EventDatastoreImpl.loadEventByID(eventId);
        return event;
    }

    public static void disableEventControls(final String userId, final String eventId) throws DataNotFoundException {
        //TODO: Validate that the user has permissions to modify event, from the EventUser table
        EventDatastoreImpl.disableEventControlFlag(eventId);
    }

    @XmlRootElement
    public static class MinEvent{
        public String id;
        public String name;

        private MinEvent(String id, String name){
            this.id = id;
            this.name = name;
        }

        private MinEvent(){};
    }

}
