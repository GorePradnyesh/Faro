package com.zik.faro.persistence.datastore;


import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Key;
import com.zik.faro.data.Event;


public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.

    //TODO: Add exception handling for the operations

    public static Key<Event> storeEvent(final Event event){
        Key<Event> key = ofy().save().entity(event).now();
        System.out.println("Stored");
        return key;
    }

    public static Event loadEvent(final Key<Event> key){
        Event event = ofy().load().key(key).now();
        return event;
    }
}
