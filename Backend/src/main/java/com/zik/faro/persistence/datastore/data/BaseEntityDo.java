package com.zik.faro.persistence.datastore.data;

import com.googlecode.objectify.annotation.Id;

public class BaseEntityDo {
	@Id
	private String id;
	private Long version;
	
	protected BaseEntityDo(){
	}
	protected BaseEntityDo(String id, Long version){
		this.id = id;
		this.version= version;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
}
