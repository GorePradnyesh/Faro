package com.zik.faro.notifications.handler;

import com.zik.faro.commons.Constants;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.notifications.firebase.NotificationPayload;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

public class ActivityNotificationHandler extends BaseNotificationHandler {
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();

	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public void createActivityNotification(ActivityDo activity, EventDo eventDo){
		String messageBody = getActivityName(activity)+" Activity just got added! Click to view details";
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_CREATED);
	}
	
	public void updateActivityNotification(ActivityDo activity, EventDo eventDo){
		String messageBody = getActivityName(activity)+" activity details have changed - Click to view updated details";
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_GENERIC);
	}
	
	public void deleteActivityNotification(ActivityDo activity, EventDo eventDo){
		String messageBody = "Uh-Oh ! "+getActivityName(activity)+" Activity was cancelled" ;
		sendActivityNotification(activity, eventDo, messageBody, Constants.NOTIFICATION_TYPE_ACTIVITY_DELETED);
	}
	
	private String getActivityName(ActivityDo activity){
		return (activity.getName() != null && !activity.getName().isEmpty()) ? activity.getName() : "";
	}
	
	private void sendActivityNotification(ActivityDo activity, EventDo eventDo, String body, String type){
		NotificationPayload notificationPayload = new NotificationPayload();
		notificationPayload.setTitle(eventDo.getEventName());
		notificationPayload.setBody(body);
		notificationPayload.setClick_action(Constants.CLICK_ACTION_DEFAULT);
		DataPayload dataPayload = new DataPayload();
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, activity.getEventId());
		dataPayload.addKVPair(Constants.NOTIFICATION_ACTIVITYID_CONST, activity.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, activity.getVersion().toString());
		try {
			FirebaseHTTPRequest request = createDataAndNotificationMessage(
					Constants.FARO_EVENT_TOPIC_CONST+activity.getEventId(), notificationPayload, dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
