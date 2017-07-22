package com.zik.faro.notifications.firebase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.zik.faro.commons.Constants;

public class DataPayload {
	private Map<String,String> faroData;
	
	public DataPayload(){
		faroData = new HashMap<String,String>();
	}
	
	public DataPayload(String title, String body, String clickAction){
		faroData = new HashMap<String,String>();
		faroData.put(Constants.NOTIFICATION_TITLE_CONST, title);
		faroData.put(Constants.NOTIFICATION_BODY_CONST, body);
		faroData.put(Constants.NOTIFICATION_CLICK_ACTION_CONST, clickAction);
	}
	
	public Map<String,String> getMap(){
		return faroData;
	}
	
	public void setMap(Map<String,String> kvMap){
		this.faroData = kvMap;
	}
	
	public void addKVPair(String key, String value){
		this.faroData.put(key, value);
	}
	
	// 07/22/2017 - Moving notification payload into data(to avoid notification to user who triggered it), helpers for the same
	public void setTitle(String title){
		this.faroData.put(Constants.NOTIFICATION_TITLE_CONST, title);
	}
	public void setBody(String body){
		this.faroData.put(Constants.NOTIFICATION_BODY_CONST, body);
	}
	public void setClickAction(String clickAction){
		this.faroData.put(Constants.NOTIFICATION_CLICK_ACTION_CONST, clickAction);
	}
}
