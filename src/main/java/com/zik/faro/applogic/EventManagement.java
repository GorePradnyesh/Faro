package com.zik.faro.applogic;

import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.data.Event;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;

import javax.xml.bind.annotation.XmlRootElement;

//TODO: better name ?
public class EventManagement {

    public static MinEvent createEvent(final EventCreateData eventCreateData, final String UserId){
        /*Create a new event with the provided data, with the FALSE control flag.*/
        Event newEvent = new Event(eventCreateData.eventName, eventCreateData.startDate,
                eventCreateData.endDate, false, eventCreateData.expenseGroup, eventCreateData.location);
        EventDatastoreImpl.storeEvent(newEvent);

        return new MinEvent(newEvent.getEventId(), newEvent.getEventName());
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
