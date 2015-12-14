package com.zik.faro.applogic;

import java.util.List;

import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.EventUserDatastoreImpl;

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
	
	public static void storeEventUser(final String eventId, final String faroUserId){
		EventUserDatastoreImpl.storeEventUser(eventId, faroUserId);
	}
	
	public static List<EventUserDo> getEventsByFaroUser(final String faroUserId){
		return EventUserDatastoreImpl.loadEventUserByFaroUser(faroUserId);
	}
	
	public static void removeEventUser(final String eventId, final String faroUserId){
		EventUserDatastoreImpl.deleteEventUser(eventId, faroUserId);
	}
	
}
