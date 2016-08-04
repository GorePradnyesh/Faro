package com.zik.faro.applogic;

import java.util.List;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;
import com.zik.faro.persistence.datastore.data.EventUserDo;

public class EventUserManagement {
	public static InviteeList getEventInvitees(final String eventId){
		List<EventUserDo> eventUsers = EventUserDatastoreImpl.loadEventUserByEvent(eventId);
		InviteeList invitees = new InviteeList();
		for(EventUserDo user : eventUsers){
			invitees.addUserStatus(new MinUser(user.getFaroUser().getFirstName(),
					user.getFaroUser().getLastName(),
					user.getFaroUser().getEmail()), user.getInviteStatus());
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
		return EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
	}
	
	public static void removeEventUser(final String eventId, final String faroUserId){
		EventUserDatastoreImpl.deleteEventUser(eventId, faroUserId);
	}
	
	public static void updateEventUserInviteStatus(final String eventId, final String faroUserId, EventInviteStatus inviteStatus) throws DataNotFoundException, DatastoreException{
		EventUserDatastoreImpl.updateActivity(new EventUserDo(eventId, faroUserId, null, inviteStatus));
	}
	
	public static void updateEventUserOwnerId(final String eventId, final String faroUserId, String ownerId) throws DataNotFoundException, DatastoreException{
		EventUserDatastoreImpl.updateActivity(new EventUserDo(eventId, faroUserId, ownerId, null));
	}
}
