package com.zik.faro.notifications.handler;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestDataOnly;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestNotificationOnly;
import com.zik.faro.notifications.firebase.NotificationPayload;

public class BaseNotificationHandler {
	protected FirebaseHTTPRequest createNotificationMessage(String to, 
			NotificationPayload notificationPayload) throws FirebaseNotificationException{
		FirebaseHTTPRequest request = new FirebaseHTTPRequestNotificationOnly(to);
		request.setNotificationPayload(notificationPayload);
		return request;
	}
	
	protected FirebaseHTTPRequest createDataMessage(String to, 
			NotificationPayload notificationPayload) throws FirebaseNotificationException{
		FirebaseHTTPRequest request = new FirebaseHTTPRequestDataOnly(to);
		request.setNotificationPayload(notificationPayload);
		return request;
	}
	
	protected FirebaseHTTPRequest createDataAndNotificationMessage(String to, 
			NotificationPayload notificationPayload, DataPayload dataPayload) 
					throws FirebaseNotificationException{
		FirebaseHTTPRequest request = new FirebaseHTTPRequestDataOnly(to);
		request.setNotificationPayload(notificationPayload);
		request.setDataPayload(dataPayload);
		return request;
	}
	
}
