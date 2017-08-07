package com.zik.faro.notifications.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.Constants;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

public class ActivityNotificationHandler extends BaseNotificationHandler {
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();
	private  static final Logger logger = LoggerFactory.getLogger(ActivityNotificationHandler.class);
	
	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public void createActivityNotification(ActivityDo activity, EventDo eventDo, String userId){
		String messageBody = getActivityName(activity)+" Activity just got added! Click to view details";
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_CREATED, userId);
	}
	
	public void updateActivityNotification(ActivityDo activity, EventDo eventDo, String userId){
		String messageBody = getActivityName(activity)+" activity details have changed - Click to view updated details";
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_GENERIC, userId);
	}
	
	public void deleteActivityNotification(ActivityDo activity, EventDo eventDo, String userId){
		String messageBody = "Uh-Oh ! "+getActivityName(activity)+" Activity was cancelled" ;
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_DELETED, userId);
	}
	
	private String getActivityName(ActivityDo activity){
		return (activity.getName() != null && !activity.getName().isEmpty()) ? activity.getName() : "";
	}
	
	private void sendActivityNotification(ActivityDo activity, EventDo eventDo, String body, String type, String userId){
		DataPayload dataPayload = new DataPayload(eventDo.getEventName(), body, Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, activity.getEventId());
		dataPayload.addKVPair(Constants.NOTIFICATION_ACTIVITYID_CONST, activity.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, activity.getVersion().toString());
		dataPayload.addKVPair(Constants.NOTIFICATION_USER_CONST, userId);
		try {
			FirebaseHTTPRequest request = createDataMessage(
					Constants.FARO_EVENT_TOPIC_CONST+activity.getEventId(), dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			logger.error("Notification Failure", e);
		}
	}
	
}
