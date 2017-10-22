package com.zik.faro.persistence.datastore.data;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.annotation.Entity;
import com.zik.faro.data.Group;

@Entity
@XmlRootElement
public class GroupDo extends BaseEntityDo{
	private String image;
	private String groupName;
	private Set<String> participants;
	private Set<String> admins;
	
	public GroupDo(){
		super();
	}
	
	public void createIdAndInitialize(){
		setId(UUID.randomUUID().toString());
		setVersion(1L);
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Set<String> getParticipants() {
		return participants;
	}
	public void setParticipants(Set<String> participants) {
		this.participants = participants;
	}
	public Set<String> getAdmins() {
		return admins;
	}
	public void setAdmins(Set<String> admins) {
		this.admins = admins;
	}
	
	
}
