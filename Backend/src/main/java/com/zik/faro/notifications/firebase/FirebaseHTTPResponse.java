package com.zik.faro.notifications.firebase;

public class FirebaseHTTPResponse {
	
	private Long message_id;
	private String error;
	private int statusCode;
	// For create topic
	private Error[] results;
	
	public static class Error{
		private String error;

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
	}
	
	public Long getMessage_id() {
		return message_id;
	}
	public void setMessage_id(Long message_id) {
		this.message_id = message_id;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
}
