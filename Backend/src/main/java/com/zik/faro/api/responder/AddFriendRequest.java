package com.zik.faro.api.responder;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

//TODO: Remove if not required and use list directly.
@XmlRootElement
public class AddFriendRequest {
	private List<String> friendIds;
	
	public AddFriendRequest(){}

	public List<String> getFriendIds() {
		return friendIds;
	}

	public void setFriendIds(List<String> friendIds) {
		this.friendIds = friendIds;
	}
}
