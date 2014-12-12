package com.zik.faro.persistence.datastore;


import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Key;
import com.zik.faro.data.Event;


public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.

    //TODO: Add exception handling for the operations

    public static void storeEvent(final Event event){
        DatastoreObjectifyDAL.storeObject(event);
    }

    public static Event loadEvent(final String eventId){
        Event event = DatastoreObjectifyDAL.loadObject(eventId, Event.class);
        return event;
    }
}
