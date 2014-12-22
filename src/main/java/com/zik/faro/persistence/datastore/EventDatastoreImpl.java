package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.
    //TODO: Add exception handling for the operations

    public static void storeEvent(final Event event){
        DatastoreObjectifyDAL.storeObject(event);
    }

    public static Event loadEventByID(final String eventId){
        Event event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
        return event;
    }

    public static void disableEventControlFlag(final String eventId) throws DataNotFoundException{
        DataNotFoundException ex = ofy().transact(new Work<DataNotFoundException>(){
            public DataNotFoundException run() {
                Event event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
                if(event == null){
                    return new DataNotFoundException("Event not found " + eventId);
                }
                event.setControlFlag(true);
                DatastoreObjectifyDAL.storeObject(event);
                return null;
            }
        });
        if(ex != null){
            throw ex;
        }
    }
}
