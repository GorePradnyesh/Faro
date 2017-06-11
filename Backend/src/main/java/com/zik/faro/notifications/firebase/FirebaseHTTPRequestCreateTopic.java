package com.zik.faro.notifications.firebase;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;

public class FirebaseHTTPRequestCreateTopic extends FirebaseHTTPRequest{
	public FirebaseHTTPRequestCreateTopic(String to, String[] registration_tokens) {
		super(to, registration_tokens);
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
