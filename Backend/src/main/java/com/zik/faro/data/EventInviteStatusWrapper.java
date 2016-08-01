package com.zik.faro.data;

import com.zik.faro.data.user.EventInviteStatus;

public class EventInviteStatusWrapper {
	private Event event;
	private EventInviteStatus inviteStatus;
	
	public EventInviteStatusWrapper(Event event, EventInviteStatus inviteStatus){
		this.event = event;
		this.inviteStatus = inviteStatus;
	}
	
	public EventInviteStatusWrapper(){}
	
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	public EventInviteStatus getInviteStatus() {
		return inviteStatus;
	}
	public void setInviteStatus(EventInviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}
}
