package com.zik.faro.notifications.firebase;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;

public class FirebaseHTTPRequestNotificationOnly extends FirebaseHTTPRequest{
	public FirebaseHTTPRequestNotificationOnly(String to) {
		super(to);
	}
	
	public FirebaseHTTPRequestNotificationOnly(String[] registrationIds) {
		super(registrationIds);
	}

	@Override
	public void setNotificationPayload(NotificationPayload notificationPayload) {
		this.notification = notificationPayload;
	}

	@Override
	public void setDataPayload(DataPayload dataPayload) throws FirebaseNotificationException{
		throw new FirebaseNotificationException("Data Payload not allowed inside notification-type of notications");
	}
}
