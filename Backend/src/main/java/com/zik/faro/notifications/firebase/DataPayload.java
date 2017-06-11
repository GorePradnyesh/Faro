package com.zik.faro.notifications.firebase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataPayload {
	private Map<String,String> kvMap;
	
	public DataPayload(){
		kvMap = new HashMap<String,String>();
	}
	
	public Map<String,String> getMap(){
		return kvMap;
	}
	
	public void setMap(Map<String,String> kvMap){
		this.kvMap = kvMap;
	}
	
	public void addKVPair(String key, String value){
		this.kvMap.put(key, value);
	}
	
}
