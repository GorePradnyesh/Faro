package com.zik.faro.persistence.datastore;


import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class EventUserDatastoreImpl {

    public static final String EVENT_REF_FIELD_NAME = "eventRef";
    public static final String FARO_USER_REF_FIELD_NAME = "faroUserRef";

    public static void storeEventUser(final String eventId, final String faroUserId){
        EventUserDo eventUserRelation = new EventUserDo(eventId, faroUserId);
        DatastoreObjectifyDAL.storeObject(eventUserRelation);
    }
    
    // overloaded method to store ownerId in the the relation.
    // should only be called while new event is being created since that is the time
    // we need to save user creating event as owner
    public static void storeEventUser(final String eventId, final String faroUserId, String ownerId, EventInviteStatus inviteStatus){
    	EventUserDo eventUserRelation = new EventUserDo(eventId, faroUserId, ownerId, inviteStatus);
    	DatastoreObjectifyDAL.storeObject(eventUserRelation);
    }

	public static EventUserDo loadEventUser(final String eventId, final String faroUserId){
        Ref<EventDo> eventRef = DatastoreObjectifyDAL.getRefForClassById(eventId, EventDo.class);
        Ref<FaroUserDo> faroUserRef = DatastoreObjectifyDAL.getRefForClassById(faroUserId, FaroUserDo.class);
        
        Query<EventUserDo> eventUserQuery = ofy().load().type(EventUserDo.class);
        eventUserQuery = eventUserQuery.filter(EVENT_REF_FIELD_NAME, eventRef);
        eventUserQuery = eventUserQuery.filter(FARO_USER_REF_FIELD_NAME, faroUserRef);
        // Return the first result since there should be another record with same key.
        return eventUserQuery.first().now();
     }
    
    
    
    public static List<EventUserDo> loadEventUserByEvent(final String eventId){
        List<EventUserDo> eventUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(EVENT_REF_FIELD_NAME,
                        EventDo.class,
                        eventId,
                        EventUserDo.class);
        return eventUserList;
    }

    public static void deleteEventUserByEvent(final String eventId){
        DatastoreObjectifyDAL.deleteObjectsByIndexedRefFieldEQ(EVENT_REF_FIELD_NAME,
                EventDo.class,
                eventId,
                EventUserDo.class);
    }


    public static List<EventUserDo> loadEventUserByFaroUser(final String faroUserId){
        List<EventUserDo> eventUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(FARO_USER_REF_FIELD_NAME,
                        FaroUserDo.class,
                        faroUserId,
                        EventUserDo.class);
        return eventUserList;
    }
    
    public static void deleteEventUser(final String eventId, final String faroUserId){
    	EventUserDo user = new EventUserDo(eventId, faroUserId);
    	DatastoreObjectifyDAL.delelteObjectById(user.getId(), EventUserDo.class);
    }
    
    public static EventUserDo updateActivity(final EventUserDo eventUserDo) throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	Work w = new Work<TransactionResult<EventUserDo>>() {
	        public TransactionResult<EventUserDo> run() {
	        	// Read from datastore
	        	EventUserDo eventUserFromDatastore = null;
				try {
					eventUserFromDatastore = DatastoreObjectifyDAL.loadObjectById(eventUserDo.getId(), EventUserDo.class);
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventUserDo>(null,TransactionStatus.DATANOTFOUND);
				}
	        	
	            // Modify.
				if(eventUserDo.getInviteStatus() != null){
					eventUserFromDatastore.setInviteStatus(eventUserDo.getInviteStatus());
				}
				
				if(eventUserDo.getOwnerId() != null){
					eventUserFromDatastore.setOwnerId(eventUserDo.getOwnerId());
				}
				
	            // Store
	            DatastoreObjectifyDAL.storeObject(eventUserFromDatastore);
	            
	            return new TransactionResult<EventUserDo>(eventUserFromDatastore,TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<EventUserDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
    	return result.getEntity();
    }
}
