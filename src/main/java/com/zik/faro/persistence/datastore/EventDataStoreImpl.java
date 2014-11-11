package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.zik.faro.data.Event;
import static com.googlecode.objectify.ObjectifyService.ofy;


public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.

    //TODO: Add exception handling for the operations

    public static void storeEvent(final Event event){
        ofy().save().entity(event).now();
        System.out.println("Stored");
    }

    public static Event loadEvent(final String eventId){
        Key<Event> eventKey = Key.create(Event.class, eventId);
        //Event event = ofy().load().key(eventKey).now();
        Event event = ofy().load().type(Event.class).id(eventId).now();
        return event;
    }
}
