package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.handler.EventNotificationHandler;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class EventManagement {
	
	public static EventNotificationHandler eventNotificationHandler = new EventNotificationHandler();
	
	public static Event createEvent(final String userId, final Event ev) {
        // clientEvent object created with client constructor needs to be passed through
		// server constructor to generate eventId and other defaults invisible to client
		Event event = new Event(ev.getEventName(), ev.getStartDate(), ev.getEndDate(),
        		ev.getEventDescription(), ev.getControlFlag(), ev.getExpenseGroup(), 
        		ev.getLocation(), ObjectStatus.OPEN, new Assignment(), userId);
        EventDatastoreImpl.storeEvent(userId, ConversionUtils.toDo(event));
        try{
        	List<String> faroUserTokens = UserDatastoreImpl.loadFaroUserById(userId).getTokens();
        	eventNotificationHandler.subscribeToTopic(event.getId(), faroUserTokens.toArray(new String[0]));
        }catch(Exception e){
        	e.printStackTrace();
        }
        return event;
    }

    public static void deleteEvent(final String eventId, final String userId) throws DataNotFoundException {
        EventDo eventDo = EventDatastoreImpl.loadEventByID(eventId);
    	EventDatastoreImpl.deleteEvent(eventId);
        try {
			eventNotificationHandler.deleteEventNotification(eventDo, userId);
		} catch (FirebaseNotificationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static EventInviteStatusWrapper getEventDetails(final String userId, final String eventId) throws DataNotFoundException{
        EventUserDo eventUserDo = EventUserDatastoreImpl.loadEventUser(eventId, userId);
        return new EventInviteStatusWrapper(ConversionUtils.fromDo(eventUserDo.getEvent()), eventUserDo.getInviteStatus());
    }
    
    public static Event updateEvent(final Event updateObj, final String eventId, final String userId) throws DataNotFoundException, DatastoreException, UpdateVersionException {
        //TODO: Validate that the user has permissions to modify event, from the EventUser table
    	//TODO: Validate if the user is the owner of the event
    	EventDo eventDo = ConversionUtils.toDo(updateObj);
    	EventDo updatedEvent = EventDatastoreImpl.updateEvent(eventId, eventDo);
    	try {
    		// TODO: For now keeping it generic. Need to filter based on properties updated
    		eventNotificationHandler.updateEventNotificationGeneric(eventDo, userId);
		} catch (FirebaseNotificationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return ConversionUtils.fromDo(updatedEvent);
    }
    
    public static void addFriendToEvent(final String eventId, final String userId, 
    		final AddFriendRequest friendRequest) throws DataNotFoundException, IllegalDataOperation{
    	for(String friendId : friendRequest.getFriendIds()){
    		// Create friend if not present and establish friend relation if not present
    		// TODO: If friend is not in the system, then we need to create a FaroUser with friend's email
    		// and send out the invite to him and after that establish friend relation with a "NOTACCEPTED" kind of state.
    		// Once user accepts invitation and joins Faro this has to be updated.
    		FriendManagement.createFriendRelation(userId, friendId);
        	// Add to event invitees
        	EventUserManagement.storeEventUser(eventId, friendId);
        	EventInviteStatusWrapper eventWrapper = EventManagement.getEventDetails(userId, eventId);
        	try {
				eventNotificationHandler.inviteFriendToEventNotification(eventWrapper.getEvent(), friendId, userId);
			} catch (FirebaseNotificationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public static List<EventInviteStatusWrapper> getEvents(final String faroUserId){
    	
    	List<EventUserDo> eventUsers = EventUserManagement.getEventsByFaroUser(faroUserId);
		
		List<EventInviteStatusWrapper> events = new ArrayList<EventInviteStatusWrapper>();
		for(EventUserDo eventUser : eventUsers){
 			events.add(new EventInviteStatusWrapper(ConversionUtils.fromDo(eventUser.getEvent()), eventUser.getInviteStatus()));
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
