package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.DatastoreObjectifyDAL;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;

public class EventManagement {

	public static Event createEvent(final String userId, final EventCreateData eventCreateData){
        /*Create a new event with the provided data, with the FALSE control flag.*/
        Event newEvent = new Event(eventCreateData.getEventName(), eventCreateData.getStartDate(),
                eventCreateData.getEndDate(), false, eventCreateData.getExpenseGroup(), eventCreateData.getLocation());
        EventDatastoreImpl.storeEvent(userId, newEvent);
        //TODO: send out notifications t
        return newEvent;
    }

    public static Event getEventDetails(final String userId, final String eventId) throws DataNotFoundException{
        Event event = EventDatastoreImpl.loadEventByID(eventId);
        return event;
    }

    public static void disableEventControls(final String userId, final String eventId) throws DataNotFoundException, DatastoreException {
        //TODO: Validate that the user has permissions to modify event, from the EventUser table
    	//TODO: Validate if the user is the owner of the event
        EventDatastoreImpl.disableEventControlFlag(eventId);
    }
    
    public static void addFriendToEvent(final String eventId, final String userId, 
    		final AddFriendRequest friendRequest) throws DataNotFoundException, IllegalDataOperation{
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
 		Collection<Event> events = DatastoreObjectifyDAL.
 				loadMultipleObjectsByIdSync(eventIds, Event.class).values();
 		return new ArrayList<Event>(events);
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
