package com.zik.faro.persistence.datastore;


import com.zik.faro.data.Event;


public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.
    //TODO: Add exception handling for the operations

    public static void storeEvent(final Event event){
        DatastoreObjectifyDAL.storeObject(event);
    }

    public static Event loadEventByID(final String eventId){
        Event event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
        return event;
    }
}
