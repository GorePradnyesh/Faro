package com.zik.faro.notifications.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.Constants;
import com.zik.faro.notifications.NotificationClient;
import com.zik.faro.notifications.NotificationClientFactory;
import com.zik.faro.notifications.firebase.DataPayload;
import com.zik.faro.notifications.firebase.FirebaseHTTPRequest;
import com.zik.faro.notifications.firebase.FirebaseHTTPResponse;
import com.zik.faro.notifications.firebase.NotificationPayload;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;

public class PollNotificationHandler extends BaseNotificationHandler{
	private NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse> notificationClient = 
			NotificationClientFactory.getInstance().getNotificationClient();
	private  static final Logger logger = LoggerFactory.getLogger(PollNotificationHandler.class);
	
	
	@Override
	protected NotificationClient<FirebaseHTTPRequest, FirebaseHTTPResponse> getNotificationClient() {
		return notificationClient;
	}
	
	public void createPollNotification(PollDo poll, EventDo eventDo, String userId){
		String messageBody = "New poll "+getPollName(poll)+" created\n Cast your vote, you must";
		sendPollNotification(poll, eventDo, messageBody, Constants.NOTIFICATION_TYPE_POLL_CREATED, userId);
	}
	
	public void updatePollNotification(PollDo poll, EventDo eventDo, String userId){
		String messageBody = "Poll "+getPollName(poll)+" has exciting new options\n Click to view options in case you want to change your vote";
		sendPollNotification(poll, eventDo, messageBody, Constants.NOTIFICATION_TYPE_POLL_GENERIC, userId);
	}
	
	public void closePollNotification(PollDo poll, EventDo eventDo, String userId){
		String messageBody = "Poll "+getPollName(poll)+" - A winner we have!";
		sendPollNotification(poll, eventDo, messageBody, Constants.NOTIFICATION_TYPE_POLL_CLOSED, userId);
	}
	
	public void deletePollNotification(PollDo poll, EventDo eventDo, String userId){
		String messageBody = "Poll "+getPollName(poll)+" has been deleted :(";
		sendPollNotification(poll, eventDo, messageBody, Constants.NOTIFICATION_TYPE_POLL_DELETED, userId);
	}
	
	private String getPollName(PollDo poll){
		return (poll.getDescription() != null && !poll.getDescription().isEmpty()) ? poll.getDescription() : "";
	}
	
	private void sendPollNotification(PollDo poll, EventDo eventDo, String body, String type, String userId){
		DataPayload dataPayload = new DataPayload(eventDo.getEventName(), body, Constants.CLICK_ACTION_DEFAULT);
		dataPayload.addKVPair(Constants.NOTIFICATION_TYPE_CONST, type);
		dataPayload.addKVPair(Constants.NOTIFICATION_EVENTID_CONST, poll.getEventId());
		dataPayload.addKVPair(Constants.NOTIFICATION_POLLID_CONST, poll.getId());
		dataPayload.addKVPair(Constants.NOTIFICATION_VERSION_CONST, poll.getVersion().toString());
		dataPayload.addKVPair(Constants.NOTIFICATION_USER_CONST, userId);
		try {
			FirebaseHTTPRequest request = createDataMessage(
					Constants.FARO_EVENT_TOPIC_CONST+poll.getEventId(), dataPayload);
			notificationClient.send(request);
		} catch (Exception e) {
			logger.error("Notification Failure", e);
		}
	}
}
