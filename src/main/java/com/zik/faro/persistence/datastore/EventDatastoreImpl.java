package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.Event;

public class EventDatastoreImpl {       
    public static void storeEvent(final String userId, final Event event){
    	DatastoreObjectifyDAL.storeObject(event);
    	// Creating event user relation if event creation succeeds
    	EventUserDatastoreImpl.storeEventUser(event.getEventId(), userId, userId);
    }
    
    // For use cases where we do not need to store eventuser relation
    public static void storeEventOnly(final Event event){
    	DatastoreObjectifyDAL.storeObject(event);
    }

    public static Event loadEventByID(final String eventId) throws DataNotFoundException{
        Event event = DatastoreObjectifyDAL.loadObjectById(eventId, Event.class);
        return event;
    }

    public static void disableEventControlFlag(final String eventId) throws DataNotFoundException, DatastoreException{
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
        DatastoreUtil.processResult(result);
    }
    
//    public static List<Event> getEventsOfFaroUser(final String faroUserId){
//    	List<EventUser> events = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
//    	for(EventUser event : events){
//    }
}
