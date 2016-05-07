package com.zik.faro.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.data.user.InviteStatus;

//@XmlRootElement
public class EventInviteStatusWrapper {
	private Event event;
	private InviteStatus inviteStatus;
	
	public EventInviteStatusWrapper(Event event, InviteStatus inviteStatus){
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
	public InviteStatus getInviteStatus() {
		return inviteStatus;
	}
	public void setInviteStatus(InviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}
}
