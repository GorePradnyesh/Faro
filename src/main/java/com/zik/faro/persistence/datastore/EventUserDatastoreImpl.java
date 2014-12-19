package com.zik.faro.persistence.datastore;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.user.FaroUser;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class EventUserDatastoreImpl {

    public static final String EVENT_REF_FIELD_NAME = "eventRef";
    public static final String FARO_USER_REF_FIELD_NAME = "faroUserRef";

    public static void storeEventUser(final String eventId, final String faroUserId){
        EventUser eventUserRelation = new EventUser(eventId, faroUserId);
        DatastoreObjectifyDAL.storeObject(eventUserRelation);
    }

    public static EventUser loadEventUser(final String eventId, final String faroUserId){
        Ref<Event> eventRef = DatastoreObjectifyDAL.getRefForClassById(eventId, Event.class);
        Ref<FaroUser> faroUserRef = DatastoreObjectifyDAL.getRefForClassById(faroUserId, FaroUser.class);

        Query<EventUser> eventUserQuery = ofy().load().type(EventUser.class);
        eventUserQuery = eventUserQuery.filter(EVENT_REF_FIELD_NAME, eventRef);
        eventUserQuery = eventUserQuery.filter(FARO_USER_REF_FIELD_NAME, faroUserRef);
        List<EventUser> eventUserList = eventUserQuery.list();
        if(eventUserList.size() > 1){
            //logger.warn("Got more than one result for eventId and farouserID");
        }
        return eventUserList.get(0);
    }
}
