package com.zik.faro.data;

import java.util.List;

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
