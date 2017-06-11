package com.zik.faro.notifications.firebase;

public enum FirebaseHTTPResponseError {
	MISSING_REGISTRATION_TOKEN("error:MissingRegistration"),
	INVALID_REGISTRATION_TOKEN("error:InvalidRegistration"),
	UNREGISTERED_DEVICE("error:NotRegistered"),
	INVALID_PACKAGE_NAME("error:InvalidPackageName"),
	MISMATCHED_SENDER("error:MismatchSenderId"),
	INVALID_PARAMETERS("error:InvalidParameters"),
	MESSAGE_TOO_BIG("error:MessageTooBig"),
	INVALID_DATA_KEY("error:InvalidDataKey"),
	INVALID_TIME_TO_LIVE("error:InvalidTtl"),
	TIMEOUT("error:Unavailable"),
	INTERNAL_SERVER_ERROR("error:InternalServerError"),
	DEVICE_MESSAGE_RATE_EXCEEDED("error:DeviceMessageRateExceeded"),
	TOPICS_MESSAGE_RATE_EXCEEDED("error:TopicsMessageRateExceeded"),
	INVALID_APNS_CREDENTIAL("error:InvalidApnsCredential");
	
	private String message;
	
	FirebaseHTTPResponseError(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
