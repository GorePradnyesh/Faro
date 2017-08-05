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
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class AssignmentNotificationHandler extends BaseNotificationHandler{
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();
	
	private  static final Logger logger = LoggerFactory.getLogger(AssignmentNotificationHandler.class);
	
	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public void pendingAssignmentNotification(EventDo event, ActivityDo activity, String faroUserId, String notificationTriggererUserId){
		sendAssignmentNotification(activity, event, "OOPS! You missed some of your assignments", 
				Constants.NOTIFICATION_TYPE_ASSIGNMENT_PENDING, faroUserId, notificationTriggererUserId);
	}
	
	public void assignmentCreatedNotification(EventDo event, ActivityDo activity, String faroUserId, String notificationTriggererUserId){
		sendAssignmentNotification(activity, event, "Well Well Well! You have been assigned items for the event :)"
				+ "\n Complete them you must", 
				Constants.NOTIFICATION_TYPE_ASSIGNMENT_CREATED, faroUserId, notificationTriggererUserId);
	}
	
	private void sendAssignmentNotification(ActivityDo activity, EventDo eventDo, String body, String type, String faroUserId
			, String notificationTriggererUserId){
		DataPayload dataPayload = new DataPayload(eventDo.getEventName(), body, Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, activity.getEventId());
		if(activity == null){
			dataPayload.addKVPair(Constants.NOTIFICATION_ACTIVITYID_CONST, null);
			dataPayload.addKVPair(Constants.NOTIFICATION_ASSIGNMENTID_CONST, eventDo.getAssignment().getId());
			dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, eventDo.getVersion().toString());
		}else{
			dataPayload.addKVPair(Constants.NOTIFICATION_ACTIVITYID_CONST, activity.getId());
			dataPayload.addKVPair(Constants.NOTIFICATION_ASSIGNMENTID_CONST, activity.getAssignment().getId());
			dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, activity.getVersion().toString());
		}
		dataPayload.addKVPair(Constants.NOTIFICATION_USER_CONST, notificationTriggererUserId);
		try {
			FirebaseHTTPRequest request = createDataMessage(
					Constants.FARO_EVENT_TOPIC_CONST+encodeTo(faroUserId), dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			logger.error("Notification Failure", e);
		}
	}
}
