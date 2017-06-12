package com.zik.faro.notifications.handler;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestCreateTopic;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequestDataAndNotification;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.notifications.firebase.NotificationPayload;

public class EventNotificationHandler extends BaseNotificationHandler{
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();
	
	public void createEventNotification(String eventName, String topic) throws FirebaseNotificationException{
		// Notification Only
		NotificationPayload payload = new NotificationPayload("New Event Created", eventName, null);
		payload.setClick_action("");
		FirebaseHTTPRequest request = createNotificationMessage(topic, payload);
		try {
			notificationClient.send(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createTopic() throws FirebaseNotificationException{
		String[] tokens = new String[]{"Test1","Test2"};
		FirebaseHTTPRequestCreateTopic topic = new FirebaseHTTPRequestCreateTopic("/topics/faro-test-topic", tokens);
		try {
			notificationClient.createTopic(topic);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//TODO: Cleanup. Used only for testing.
	public static void main(String[] args) throws Exception{
		EventNotificationHandler test = new EventNotificationHandler();
		test.createEventNotification("testevent2", "/topics/faro-test-topic");
		test.createTopic();
		//System.out.println(jsonPayload);
	}
}
