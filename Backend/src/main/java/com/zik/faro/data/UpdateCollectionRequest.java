package com.zik.faro.data;

import java.util.List;

public class UpdateCollectionRequest<T,V> extends UpdateRequest<T> {
	private List<V> toBeAdded;
	private List<V> toBeRemoved;
	
	public UpdateCollectionRequest(){}
	

	public List<V> getToBeAdded() {
		return toBeAdded;
	}

	public void setToBeAdded(List<V> addCollection) {
		this.toBeAdded = addCollection;
	}

	public List<V> getToBeRemoved() {
		return toBeRemoved;
	}

	public void setToBeRemoved(List<V> removeCollection) {
		this.toBeRemoved = removeCollection;
	}
}
