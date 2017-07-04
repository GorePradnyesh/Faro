package com.zik.faro.notifications.handler;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestCreateTopic;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestDataAndNotification;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestDataOnly;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestNotificationOnly;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.notifications.firebase.NotificationPayload;

public abstract class BaseNotificationHandler {
	
	protected abstract NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient();
	
	public void subscribeToTopic(String topicName, String[] tokens) throws FirebaseNotificationException{
		FirebaseHTTPRequestCreateTopic topic = new FirebaseHTTPRequestCreateTopic(topicName, tokens);
		try {
			FirebaseHTTPResponse response = getNotificationClient().subscribeToTopic(topic);
			if(response.getStatusCode() != 200){
				System.out.println("Subscription request failure!");
				return;
			}
			if(response.getErrorResults() != null && response.getErrorResults().length != 0){
				System.out.println(response.getErrorResults());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void unsubscribeToTopic(String topicName, String[] tokens) throws FirebaseNotificationException{
		FirebaseHTTPRequestCreateTopic topic = new FirebaseHTTPRequestCreateTopic(topicName, tokens);
		try {
			FirebaseHTTPResponse response = getNotificationClient().unsubscribeToTopic(topic);
			if(response.getStatusCode() != 200){
				System.out.println("Subscription request failure!");
				return;
			}
			if(response.getErrorResults() != null && response.getErrorResults().length != 0){
				System.out.println(response.getErrorResults());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
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
		FirebaseHTTPRequest request = new FirebaseHTTPRequestDataAndNotification(to);
		request.setNotificationPayload(notificationPayload);
		request.setDataPayload(dataPayload);
		return request;
	}
	
}
