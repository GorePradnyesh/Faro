package com.zik.faro.commons;

public class FaroResponse<T> {
	private T entity;
	private FaroResponseStatus faroResponseStatus;
	public FaroResponse(){}
	public FaroResponse(T entity, FaroResponseStatus faroResponseStatus){
		this.entity = entity;
		this.faroResponseStatus = faroResponseStatus;
	}
	public T getEntity() {
		return entity;
	}
	public void setEntity(T entity) {
		this.entity = entity;
	}
	public FaroResponseStatus getFaroResponseStatus() {
		return faroResponseStatus;
	}
	public void setFaroResponseStatsus(FaroResponseStatus faroResponseStatus) {
		this.faroResponseStatus = faroResponseStatus;
	}
}
