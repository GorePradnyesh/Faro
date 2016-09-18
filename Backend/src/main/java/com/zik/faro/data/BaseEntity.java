package com.zik.faro.data;


public class BaseEntity {

	private String id;
	private Long version;
	
	protected BaseEntity(){
	}
	
	protected BaseEntity(String id, Long version){
		this.id = id;
		this.version = version;
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
