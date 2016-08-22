package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.persistence.datastore.data.EventDo;

public class EventDatastoreImpl {       
    public static void storeEvent(final String userId, final EventDo event){
    	DatastoreObjectifyDAL.storeObject(event);
    	// Creating event user relation if event creation succeeds. Owner will be userId too and since owner created event, he has by default accepted it
    	EventUserDatastoreImpl.storeEventUser(event.getEventId(), userId, userId, EventInviteStatus.ACCEPTED);
    }
    
    // For use cases where we do not need to store eventuser relation
    public static void storeEventOnly(final EventDo event){
    	DatastoreObjectifyDAL.storeObject(event);
    }

    public static EventDo loadEventByID(final String eventId) throws DataNotFoundException{
        EventDo event = DatastoreObjectifyDAL.loadObjectById(eventId, EventDo.class);
        return event;
    }

    public static EventDo disableEventControlFlag(final String eventId) throws DataNotFoundException, DatastoreException{
        Work w = new Work<TransactionResult<EventDo>>() {
			
			@Override
			public TransactionResult<EventDo> run() {
				EventDo event;
				try {
					event = DatastoreObjectifyDAL.loadObjectById(eventId, EventDo.class);
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.DATANOTFOUND);
				}
                event.setControlFlag(true);
                DatastoreObjectifyDAL.storeObject(event);
                return new TransactionResult<EventDo>(event, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<EventDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        return result.getEntity();
    }
    
    public static EventDo updateEvent(final String eventId, final EventDo updateObj) throws DataNotFoundException, DatastoreException{
        Work w = new Work<TransactionResult<EventDo>>() {
			
			@Override
			public TransactionResult<EventDo> run() {
				EventDo event;
				try {
					event = DatastoreObjectifyDAL.loadObjectById(eventId, EventDo.class);
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(updateObj.getEndDate() != null){
					event.setEndDate(updateObj.getEndDate());
				}
				if(updateObj.getStartDate() != null){
					event.setStartDate(updateObj.getStartDate());
				}
				if(updateObj.getEventDescription() != null){
					event.setEventDescription(updateObj.getEventDescription());
				}
				if(updateObj.getEventName() != null){
					event.setEventName(updateObj.getEventName());
				}
				if(updateObj.getLocation() != null){
					event.setLocation(updateObj.getLocation());
				}
				if(updateObj.getStatus() != null){
					event.setStatus(updateObj.getStatus());
				}
				if(updateObj.getExpenseGroup() != null){
					event.setExpenseGroup(updateObj.getExpenseGroup());
				}
                DatastoreObjectifyDAL.storeObject(event);
                return new TransactionResult<EventDo>(event, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<EventDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        return result.getEntity();
    }

    public static void deleteEvent(final String eventId) {
        DatastoreObjectifyDAL.delelteObjectById(eventId, EventDo.class);
    }

//    public static List<Event> getEventsOfFaroUser(final String faroUserId){
//    	List<EventUser> events = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
//    	for(EventUser event : events){
//    }
}
