package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.data.Event;
import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.data.EventCreateData;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.DatastoreObjectifyDAL;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;

public class EventManagement {

	public static Event createEvent(final String userId, final EventCreateData eventCreateData){
        /*Create a new event with the provided data, with the FALSE control flag.*/
        Event newEvent = new Event(eventCreateData.getEventName(), eventCreateData.getStartDate(),
                eventCreateData.getEndDate(), false, eventCreateData.getExpenseGroup(), eventCreateData.getLocation());
        
        EventDatastoreImpl.storeEvent(userId, ConversionUtils.toDo(newEvent));
        //TODO: send out notifications t
        return newEvent;
    }

    public static Event getEventDetails(final String userId, final String eventId) throws DataNotFoundException{
        EventDo event = EventDatastoreImpl.loadEventByID(eventId);
        return ConversionUtils.fromDo(event);
    }

    public static void disableEventControls(final String userId, final String eventId) throws DataNotFoundException, DatastoreException {
        //TODO: Validate that the user has permissions to modify event, from the EventUser table
    	//TODO: Validate if the user is the owner of the event
        EventDatastoreImpl.disableEventControlFlag(eventId);
    }
    
    public static void addFriendToEvent(final String eventId, final String userId, 
    		final AddFriendRequest friendRequest) throws DataNotFoundException, IllegalDataOperation{
    	FaroUserDo existingUser = UserManagement.loadFaroUser(userId);
    	
    	for(String friendId : friendRequest.getFriendIds()){
    		// Create friend if not present and establish friend relation if not present
    		// TODO: If friend is not in the system, then we need to create a FaroUser with friend's email
    		// and send out the invite to him and after that establish friend relation with a "NOTACCEPTED" kind of state.
    		// Once user accepts invitation and joins Faro this has to be updated.
    		FriendManagement.inviteFriend(existingUser.getEmail(), friendId);
        	
        	// Add to event invitees
        	EventUserManagement.storeEventUser(eventId, friendId);
    	}
    }
    
    public static List<Event> getEvents(final String faroUserId){
    	// Load first "N" event Ids for particular user
		List<EventUserDo> eventUsers = EventUserManagement.getEventsByFaroUser(faroUserId);
		List<String> eventIds = new ArrayList<String>();
 		for(EventUserDo eventUser : eventUsers){
			eventIds.add(eventUser.getEvent().getEventId());
		}
 		// Load actual events
 		Collection<EventDo> eventDos = DatastoreObjectifyDAL.
 				loadMultipleObjectsByIdSync(eventIds, EventDo.class).values();
 		List<Event> events = new ArrayList<Event>();
 		for(EventDo eventDo:eventDos){
 			events.add(ConversionUtils.fromDo(eventDo));
 		}
 		
 		return events;
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
