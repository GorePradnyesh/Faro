package com.zik.faro.notifications.firebase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataPayload {
	private Map<String,String> faroData;
	
	public DataPayload(){
		faroData = new HashMap<String,String>();
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
	
}
