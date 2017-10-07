package com.zik.faro.commons;

import javax.ws.rs.core.Response;

public class FaroErrorResponse {
	
	public int getFaroStatusCode() {
		return faroStatusCode;
	}
	public void setFaroStatusCode(int faroStatusCode) {
		this.faroStatusCode = faroStatusCode;
	}
	public String getFaroStatusMessage() {
		return faroStatusMessage;
	}
	public void setFaroStatusMessage(String faroStatusMessage) {
		this.faroStatusMessage = faroStatusMessage;
	}
    private int faroStatusCode;
    private String faroStatusMessage;
    
    public FaroErrorResponse(int faroStatusCode, String faroStatusMessage){
    	this.faroStatusCode = faroStatusCode;
    	this.faroStatusMessage = faroStatusMessage;
    }
    public FaroErrorResponse(){}
}
