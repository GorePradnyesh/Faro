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

public class EventNotificationHandler extends BaseNotificationHandler{
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();

	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public String getTopicName(String eventId){
		return Constants.FARO_EVENT_TOPIC_CONST+eventId;
	}
	
	public void updateEventNotificationGeneric(EventDo updatedEvent, String userId)throws FirebaseNotificationException{
		sendEventNotification(updatedEvent, "Event details have changed - Click to view updated details",
				Constants.NOTIFICATION_TYPE_EVENT_GENERIC, userId);
	}
	
	public void deleteEventNotification(EventDo updatedEvent, String userId)throws FirebaseNotificationException{
		sendEventNotification(updatedEvent, "Darn! Event has been cancelled",
				Constants.NOTIFICATION_TYPE_EVENT_DELETED, userId);
	}
	
	public void sendEventNotification(EventDo event, String body, String type, String userId){
		DataPayload dataPayload = new DataPayload(event.getEventName(), body, Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, event.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, event.getVersion().toString());
		dataPayload.addKVPair(Constants.NOTIFICATION_USER_CONST, userId);
		try {
			FirebaseHTTPRequest request = createDataMessage(
					Constants.FARO_EVENT_TOPIC_CONST+event.getId(), dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void inviteFriendToEventNotification(Event event, FaroUser faroUser) throws FirebaseNotificationException{
		DataPayload dataPayload = new DataPayload(event.getEventName(),"You are invited to "+ event.getEventName(), Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, Constants.NOTIFICATION_TYPE_EVENT_INVITE);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, event.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, event.getVersion().toString());
		FirebaseHTTPRequest request = createDataMessage(
				Constants.FARO_EVENT_TOPIC_CONST+faroUser.getId(), dataPayload);
		try {
			notificationClient.send(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
