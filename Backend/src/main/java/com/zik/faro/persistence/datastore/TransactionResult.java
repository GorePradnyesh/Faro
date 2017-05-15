package com.zik.faro.persistence.datastore;

public class TransactionResult<T> {
	private TransactionStatus status;
	private String message;
	private T entity;
	
	public TransactionResult(T entity, TransactionStatus status){
		this.setStatus(status);
		this.setEntity(entity);
	}
	
	public TransactionResult(T entity, TransactionStatus status, String message){
		this.setStatus(status);
		this.setEntity(entity);
		this.setMessage(message);
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
