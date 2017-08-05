package com.zik.faro.applogic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.notifications.handler.EventNotificationHandler;
import com.zik.faro.notifications.handler.PollNotificationHandler;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class EventUserManagement {
	private static EventNotificationHandler eventNotificationHandler = new EventNotificationHandler();
	private  static final Logger logger = LoggerFactory.getLogger(EventUserManagement.class);
	
	public static InviteeList getEventInvitees(final String eventId){
		List<EventUserDo> eventUsers = EventUserDatastoreImpl.loadEventUserByEvent(eventId);
		eventUsers = filterDeclinedEvents(eventUsers);
		InviteeList invitees = new InviteeList();
		for(EventUserDo user : eventUsers){
            FaroUserDo faroUser = user.getFaroUser();
			invitees.addUserStatus(new MinUser(faroUser.getFirstName(),
                    faroUser.getLastName(),
                    faroUser.getId()), user.getInviteStatus());
		}
		return invitees;
	}

	public static void deleteRelationForEvent(final String eventId) {
		EventUserDatastoreImpl.deleteEventUserByEvent(eventId);
	}
	
	public static void storeEventUser(final String eventId, final String faroUserId){
		EventUserDatastoreImpl.storeEventUser(eventId, faroUserId);
	}
	
	public static List<EventUserDo> getEventsByFaroUser(final String faroUserId){
		List<EventUserDo> eventUsers = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
		eventUsers = filterDeclinedEvents(eventUsers);
		return eventUsers;
	}
	
	public static void removeEventUser(final String eventId, final String faroUserId){
		EventUserDatastoreImpl.deleteEventUser(eventId, faroUserId);
	}
	
	public static void updateEventUserInviteStatus(final String eventId, final String faroUserId, EventInviteStatus inviteStatus) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		EventUserDatastoreImpl.updateActivity(new EventUserDo(eventId, faroUserId, null, inviteStatus));
		FaroUserDo faroUserDo = UserDatastoreImpl.loadFaroUserById(faroUserId);
		List<String> tokens = faroUserDo.getTokens();
		if(tokens != null && !tokens.isEmpty()){
			try{
				if(EventInviteStatus.ACCEPTED.equals(inviteStatus)){
					eventNotificationHandler.subscribeToTopic(eventId, 
	    					tokens.toArray(new String[0]));
				}else{
					eventNotificationHandler.unsubscribeToTopic(eventId, 
	    					tokens.toArray(new String[0]));
				}
			}catch(Exception e){
				logger.error("Subscription/Unsubscription Failure", e);
			}
		}
	}
	
	public static void updateEventUserOwnerId(final String eventId, final String faroUserId, String ownerId) throws DataNotFoundException, DatastoreException, UpdateVersionException{
		EventUserDatastoreImpl.updateActivity(new EventUserDo(eventId, faroUserId, ownerId, null));
	}
	
	private static List<EventUserDo> filterDeclinedEvents(List<EventUserDo> eventUsers){
    	if(eventUsers == null || eventUsers.isEmpty()){
    		return eventUsers;
    	}
    	List<EventUserDo> editedList = new ArrayList<EventUserDo>();
    	for(EventUserDo eventUser: eventUsers){
    		if(!eventUser.getInviteStatus().equals(EventInviteStatus.DECLINED)){
    			editedList.add(eventUser);
    		}
    	}
    	return editedList;
    }
}
