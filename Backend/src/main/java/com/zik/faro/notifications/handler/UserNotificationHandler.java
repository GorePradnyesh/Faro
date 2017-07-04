package com.zik.faro.notifications.handler;

import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.FirebaseNotificationException;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.notifications.firebase.NotificationPayload;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class UserNotificationHandler extends BaseNotificationHandler{
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();

	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public void inviteFriendToEventNotification(EventDo event, FaroUserDo faroUser) throws FirebaseNotificationException{
		sendNotification(event, faroUser, "You are invited to "+ event.getEventName(), 
				Constants.NOTIFICATION_TYPE_EVENT_INVITE);
		
	}
	
	public void pendingAssignmentNotification(EventDo event, FaroUserDo faroUser){
		sendNotification(event, faroUser, "OOPS! You missed some of your assignments", 
				Constants.NOTIFICATION_TYPE_ASSIGNMENT_PENDING);
	}
	
	public void assignmentCreatedNotification(EventDo event, FaroUserDo faroUser){
		sendNotification(event, faroUser, "Well Well Well! You have been assigned items for the event :)"
				+ "\n Complete them you must", 
				Constants.NOTIFICATION_TYPE_ASSIGNMENT_CREATED);
	}
	
	public void mediaAddedNotification(EventDo event, FaroUserDo faroUser){
		sendNotification(event, faroUser, "Click Click! New pictures have been added by "
				+faroUser.getFirstName() + ". Lets go see them!", 
				Constants.NOTIFICATION_TYPE_MEDIA_ADDED);
	}
	
	public void uploadMediaReminderNotification(EventDo event, FaroUserDo faroUser){
		sendNotification(event, faroUser, "Seemed like  a fun trip! Come on - lets upload and share some memories ", 
				Constants.NOTIFICATION_TYPE_UPLOAD_MEDIA_REMINDER);
	}
	
	public void sendNotification(EventDo event, FaroUserDo faroUser, String body, String type){
		NotificationPayload notificationPayload = new NotificationPayload();
		notificationPayload.setTitle(event.getEventName());
		notificationPayload.setBody(body);
		notificationPayload.setClick_action(Constants.CLICK_ACTION_DEFAULT);
		DataPayload dataPayload = new DataPayload();
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, event.getId());
		try {
			FirebaseHTTPRequest request = createDataAndNotificationMessage(
					Constants.FARO_EVENT_TOPIC_CONST+faroUser.getId(), notificationPayload, dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
