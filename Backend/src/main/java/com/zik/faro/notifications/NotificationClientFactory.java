package com.zik.faro.notifications;

import com.zik.faro.notifications.firebase.FirebaseNotificationClient;

public class NotificationClientFactory {
	private NotificationClient notificationClient = null;
	
	private static NotificationClientFactory instance = new NotificationClientFactory();
	
	private NotificationClientFactory() {
		notificationClient = new FirebaseNotificationClient();
	}
	
	public static NotificationClientFactory getInstance(){
		return instance;
	}
	
	public NotificationClient getNotificationClient(){
		return notificationClient;
	}
	
}
