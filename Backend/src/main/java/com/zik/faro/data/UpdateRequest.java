package com.zik.faro.data;

import java.util.HashSet;
import java.util.Set;

public class UpdateRequest<T> {
	private Set<String> updatedFields = new HashSet<String>();
	private T update;

	public UpdateRequest() {

    }

    public UpdateRequest(Set<String> updatedFields, T update) {
	    this.updatedFields = updatedFields;
	    this.update = update;
    }
	
	public Set<String> getUpdatedFields() {
		return updatedFields;
	}
	
	public void setUpdatedFields(Set<String> updatedFields) {
		this.updatedFields = updatedFields;
	}
	
	public void addUpdatedFields(String field){
		this.updatedFields.add(field);
	}
	
	public T getUpdate() {
		return update;
	}
	
	public void setUpdate(T update) {
		this.update = update;
	}	
}
