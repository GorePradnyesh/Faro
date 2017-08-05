package com.zik.faro.notifications.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.Constants;
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
	private  static final Logger logger = LoggerFactory.getLogger(BaseNotificationHandler.class);
	
	public void subscribeToTopic(String topicName, String[] tokens) throws FirebaseNotificationException{
		try {
			FirebaseHTTPRequestCreateTopic topic = new FirebaseHTTPRequestCreateTopic(Constants.FARO_EVENT_TOPIC_CONST+encodeTo(topicName), tokens);
			FirebaseHTTPResponse response = getNotificationClient().subscribeToTopic(topic);
			handleResponse(response);
		} catch (Exception e) {
			logger.error("Subscription Failure", e);
		}
	}
	
	public void unsubscribeToTopic(String topicName, String[] tokens) throws FirebaseNotificationException{
		try {
			FirebaseHTTPRequestCreateTopic topic = new FirebaseHTTPRequestCreateTopic(Constants.FARO_EVENT_TOPIC_CONST+encodeTo(topicName), tokens);
			FirebaseHTTPResponse response = getNotificationClient().unsubscribeToTopic(topic);
			handleResponse(response);
		} catch (Exception e) {
			logger.error("UnSubscription Failure", e);
		}
		
	}
	
	private void handleResponse(FirebaseHTTPResponse response){
		if(response.getStatusCode() != 200){
			logger.error(MessageFormat.format("HTTP Status Code: {0}", response.getStatusCode()));
			return;
		}
		if(response.getErrorResults() != null && response.getErrorResults().length != 0){
			logger.error("Error Results",response.getErrorResults());
		}
	}
	public String encodeTo(String to) throws UnsupportedEncodingException{
		return URLEncoder.encode(to, "UTF-8");
	}
	
	protected FirebaseHTTPRequest createNotificationMessage(String to, 
			NotificationPayload notificationPayload) throws FirebaseNotificationException{
		FirebaseHTTPRequest request = new FirebaseHTTPRequestNotificationOnly(to);
		request.setNotificationPayload(notificationPayload);
		return request;
	}
	
	protected FirebaseHTTPRequest createDataMessage(String to, 
			DataPayload dataPayload) throws FirebaseNotificationException{
		FirebaseHTTPRequest request = new FirebaseHTTPRequestDataOnly(to);
		request.setDataPayload(dataPayload);
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
