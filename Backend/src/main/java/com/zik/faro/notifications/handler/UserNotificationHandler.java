package com.zik.faro.notifications.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private  static final Logger logger = LoggerFactory.getLogger(UserNotificationHandler.class);
	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
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
		DataPayload dataPayload = new DataPayload(event.getEventName(), body, Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, event.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, event.getVersion().toString());
		dataPayload.addKVPair(Constants.NOTIFICATION_USER_CONST, faroUser.getId());
		try {
			FirebaseHTTPRequest request = createDataMessage(
					Constants.FARO_EVENT_TOPIC_CONST+encodeTo(faroUser.getId()), dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			logger.error("Notification Failure", e);
		}
	}
}
