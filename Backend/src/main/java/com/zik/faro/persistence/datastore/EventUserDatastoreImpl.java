package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.data.EventUser;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class EventUserDatastoreImpl {

    public static final String EVENT_REF_FIELD_NAME = "eventRef";
    public static final String FARO_USER_REF_FIELD_NAME = "faroUserRef";

    public static void storeEventUser(final String eventId, final String faroUserId){
        EventUser eventUserRelation = new EventUser(eventId, faroUserId);
        DatastoreObjectifyDAL.storeObject(eventUserRelation);
    }
    
    // overloaded method to store ownerId in the the relation.
    // should only be called while new event is being created since that is the time
    // we need to save user creating event as owner
    public static void storeEventUser(final String eventId, final String faroUserId, String ownerId){
    	EventUser eventUserRelation = new EventUser(eventId, faroUserId, ownerId);
    	DatastoreObjectifyDAL.storeObject(eventUserRelation);
    }

    public static EventUser loadEventUser(final String eventId, final String faroUserId){
        Ref<EventDo> eventRef = DatastoreObjectifyDAL.getRefForClassById(eventId, EventDo.class);
        Ref<FaroUserDo> faroUserRef = DatastoreObjectifyDAL.getRefForClassById(faroUserId, FaroUserDo.class);
        
        Query<EventUser> eventUserQuery = ofy().load().type(EventUser.class);
        eventUserQuery = eventUserQuery.filter(EVENT_REF_FIELD_NAME, eventRef);
        eventUserQuery = eventUserQuery.filter(FARO_USER_REF_FIELD_NAME, faroUserRef);
        // Return the first result since there should be another record with same key.
        return eventUserQuery.first().now();
     }
    
    public static List<EventUser> loadEventUserByEvent(final String eventId){
        List<EventUser> eventUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(EVENT_REF_FIELD_NAME,
                        EventDo.class,
                        eventId,
                        EventUser.class);
        return eventUserList;
    }

    public static List<EventUser> loadEventUserByFaroUser(final String faroUserId){
        List<EventUser> eventUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(FARO_USER_REF_FIELD_NAME,
                        FaroUserDo.class,
                        faroUserId,
                        EventUser.class);
        return eventUserList;
    }
    
    public static void deleteEventUser(final String eventId, final String faroUserId){
    	EventUser user = new EventUser(eventId, faroUserId);
    	DatastoreObjectifyDAL.delelteObjectById(user.getId(), EventUser.class);
    }
}
