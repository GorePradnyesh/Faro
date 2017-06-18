package com.zik.faro.notifications.firebase;

import com.zik.faro.commons.exceptions.FirebaseNotificationException;

public abstract class FirebaseHTTPRequest implements FirebaseRequest{
	
	private String to;
	private String[] registrationIds;
	private String[] registration_tokens;
	private Options options;
	protected NotificationPayload notification;
	protected DataPayload data;
	
	protected FirebaseHTTPRequest(String to) {
		this.to = to;
	}
	protected FirebaseHTTPRequest(String[] registrationIds) {
		this.registrationIds = registrationIds;
	}
	
	// For create topic
	protected FirebaseHTTPRequest(String to, String[] registration_tokens){
		this.to = to;
		this.registration_tokens = registration_tokens;
	}
	
	protected FirebaseHTTPRequest(){
		
	}
	
	public Options getOptions(){
		return options;
	}
	
	public void setOptions(Options options){
		this.options = options;
	}
	
	public NotificationPayload getNotificationPayload() {
		return notification;
	}
	public abstract void setNotificationPayload(NotificationPayload notificationPayload) throws FirebaseNotificationException;

	public DataPayload getDataPayload() {
		return data;
	}
	
	public abstract void setDataPayload(DataPayload dataPayload) throws FirebaseNotificationException;

	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}

	public String[] getRegistrationIds() {
		return registrationIds;
	}
	public void setRegistrationIds(String[] registrationIds) {
		this.registrationIds = registrationIds;
	}

	public String[] getRegistration_tokens() {
		return registration_tokens;
	}
	public void setRegistration_tokens(String[] registration_tokens) {
		this.registration_tokens = registration_tokens;
	}

	public static class Options{
		public enum Priority{
			normal,high
		}
		private String collapseKey;
		private Priority priority = Priority.normal;
		private boolean contentAvailable;
		private boolean mutableContent;
		private int timeToLive;
		private String restrictedPackageName;
		private boolean dryRun;
		
		public Options(){};
		
		public String getCollapseKey() {
			return collapseKey;
		}
		public void setCollapseKey(String collapseKey) {
			this.collapseKey = collapseKey;
		}
		public Priority getPriority() {
			return priority;
		}
		public void setPriority(Priority priority) {
			this.priority = priority;
		}
		public boolean isContentAvailable() {
			return contentAvailable;
		}
		public void setContentAvailable(boolean contentAvailable) {
			this.contentAvailable = contentAvailable;
		}
		public boolean isMutableContent() {
			return mutableContent;
		}
		public void setMutableContent(boolean mutableContent) {
			this.mutableContent = mutableContent;
		}
		public int getTimeToLive() {
			return timeToLive;
		}
		public void setTimeToLive(int timeToLive) {
			this.timeToLive = timeToLive;
		}
		public String getRestrictedPackageName() {
			return restrictedPackageName;
		}
		public void setRestrictedPackageName(String restrictedPackageName) {
			this.restrictedPackageName = restrictedPackageName;
		}
		public boolean isDryRun() {
			return dryRun;
		}
		public void setDryRun(boolean dryRun) {
			this.dryRun = dryRun;
		}
	}
	
}
