package com.zik.faro.notifications.firebase;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;

public class FirebaseHTTPRequestDataOnly extends FirebaseHTTPRequest{
	public FirebaseHTTPRequestDataOnly(String to) {
		super(to);
	}
	
	public FirebaseHTTPRequestDataOnly(String[] registrationIds) {
		super(registrationIds);
	}

	@Override
	public void setNotificationPayload(NotificationPayload notificationPayload) throws FirebaseNotificationException {
		throw new FirebaseNotificationException("Notification Payload not allowed inside Data only notication");
	}

	@Override
	public void setDataPayload(DataPayload dataPayload) {
		this.data = dataPayload;
	}
}
