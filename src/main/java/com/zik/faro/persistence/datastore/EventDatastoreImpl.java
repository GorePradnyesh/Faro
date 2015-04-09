package com.zik.faro.persistence.datastore;


import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;

public class EventDatastoreImpl {       //TODO: Have this implement a EventStore interface.
    //TODO: Add exception handling for the operations

    public static void storeEvent(final Event event){
        DatastoreObjectifyDAL.storeObject(event);
    }

    public static Event loadEventByID(final String eventId) throws DataNotFoundException{
        Event event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
        return event;
    }

    public static void disableEventControlFlag(final String eventId) throws DataNotFoundException{
        Work w = new Work<TransactionResult>() {
			
			@Override
			public TransactionResult run() {
				Event event;
				try {
					event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
				} catch (DataNotFoundException e) {
					return TransactionResult.DATANOTFOUND;
				}
                event.setControlFlag(true);
                DatastoreObjectifyDAL.storeObject(event);
                return TransactionResult.SUCCESS;
			}
		};
        TransactionResult result = DatastoreObjectifyDAL.update(w);
        if(result.equals(TransactionResult.DATANOTFOUND)){
    		//TODO change enum to send message as well to extract key of entity
    		throw new DataNotFoundException("");
    	}
    }
    
//    public static List<Event> getEventsOfFaroUser(final String faroUserId){
//    	List<EventUser> events = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
//    	for(EventUser event : events){
//    }
}
