package com.zik.faro.notifications.firebase;

public class FirebaseHTTPRequestDataAndNotification extends FirebaseHTTPRequest{

	public FirebaseHTTPRequestDataAndNotification(String to) {
		super(to);
	}
	
	public FirebaseHTTPRequestDataAndNotification(String[] registrationIds) {
		super(registrationIds);
	}

	@Override
	public void setNotificationPayload(NotificationPayload notificationPayload) {
		this.notification = notificationPayload;
	}

	@Override
	public void setDataPayload(DataPayload dataPayload) {
		this.data = dataPayload;
	}
}
