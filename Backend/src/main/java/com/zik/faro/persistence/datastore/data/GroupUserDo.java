package com.zik.faro.persistence.datastore.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

@Entity
@XmlRootElement
public class GroupUserDo {
	
	@Id
    private String id;
    @Index
    private Ref<GroupDo> groupRef;
    @Index
    private Ref<FaroUserDo> faroUserRef;
    
    public GroupUserDo(){}
    
    public GroupUserDo(final String groupId, final String faroUserId){
    	this.id = generateGroupUserId(groupId, faroUserId);
    	this.groupRef = Ref.create(Key.create(GroupDo.class, groupId));
    	this.faroUserRef = Ref.create(Key.create(FaroUserDo.class, faroUserId));
    }
    
    private String generateGroupUserId(final String groupId, final String faroUserId){
    	return groupId+"/"+faroUserId;
    }
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Ref<GroupDo> getGroupRef() {
		return groupRef;
	}

	public void setGroupRef(Ref<GroupDo> groupRef) {
		this.groupRef = groupRef;
	}

	public Ref<FaroUserDo> getFaroUserRef() {
		return faroUserRef;
	}

	public void setFaroUserRef(Ref<FaroUserDo> faroUserRef) {
		this.faroUserRef = faroUserRef;
	}
	
	public GroupDo getGroupDo(){
		return this.groupRef.get();
	}
	
	public FaroUserDo getFaroUserDo(){
		return this.faroUserRef.get();
	}

}
