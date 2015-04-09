package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.DatastoreObjectifyDAL;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;

//TODO: better name ?
//TODO: Need to add retry logic to the operations
public class EventManagement {

    public static MinEvent createEvent(final String userId, final EventCreateData eventCreateData){
        //TODO: create entry in the Event User table, before creating the event.

        /*Create a new event with the provided data, with the FALSE control flag.*/
        Event newEvent = new Event(eventCreateData.eventName, eventCreateData.startDate,
                eventCreateData.endDate, false, eventCreateData.expenseGroup, eventCreateData.location);
        EventDatastoreImpl.storeEvent(newEvent);
        //TODO: send out notifications t
        return new MinEvent(newEvent.getEventId(), newEvent.getEventName());
    }

    public static Event getEventDetails(final String userId, final String eventId){
        //TODO: Validate that the user has permissions to access eventId, from the EventUser table
        Event event = EventDatastoreImpl.loadEventByID(eventId);
        return event;
    }

    public static void disableEventControls(final String userId, final String eventId) throws DataNotFoundException {
        //TODO: Validate that the user has permissions to modify event, from the EventUser table
        EventDatastoreImpl.disableEventControlFlag(eventId);
    }
    
    public static void addFriendToEvent(final String eventId, final String userId, 
    		final AddFriendRequest friendRequest){
    	FaroUser existingUser = UserManagement.loadFaroUser(userId);
    	
    	for(String friendId : friendRequest.getFriendIds()){
    		// Create friend if not present and establish friend relation if not present
    		FriendManagement.inviteFriend(existingUser.getId(), friendId);
        	
        	// Add to event invitees
        	EventUserManagement.storeEventUser(eventId, friendId);
    	}
    }
    
    public static List<Event> getEvents(final String faroUserId){
    	// Load first "N" event Ids for particular user
		List<EventUser> eventUsers = EventUserManagement.getEventsByFaroUser(faroUserId);
		List<String> eventIds = new ArrayList<String>();
 		for(EventUser eventUser : eventUsers){
			eventIds.add(eventUser.getEvent().getEventId());
		}
 		// Load actual events
 		return (List<Event>)DatastoreObjectifyDAL.
 				loadMultipleObjectsByIdSync(eventIds, Event.class).values();
	}
    
    public static void removeAttendee(final String eventId, final String userId){
    	
    }
    
    @XmlRootElement
    public static class MinEvent{
        public String id;
        public String name;

        private MinEvent(String id, String name){
            this.id = id;
            this.name = name;
        }

        private MinEvent(){};
    }

}
