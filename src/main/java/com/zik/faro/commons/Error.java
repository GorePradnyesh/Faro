package com.zik.faro.commons;

public enum Error {
	NOT_FOUND("Data not found");
	
	private String message;
	
	Error(String message){
		this.message = message;
	}
}
