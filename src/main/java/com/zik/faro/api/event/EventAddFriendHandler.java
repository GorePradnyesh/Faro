package com.zik.faro.api.event;

import static com.zik.faro.commons.Constants.EVENT_ADD_FRIENDS_CONST;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
@Deprecated
public class EventAddFriendHandler {
	/*
	 * 
	 * HAVE MOVED THIS FUNCTIONALITY TO EVENTHANDLER.JAVA
	 * 
	 */
	@POST
	public String addFriendsToEvent(@QueryParam("Signature")final String signature,
			String data){
		if(data == null || data.isEmpty()){
			Response response = Response.status(400).entity("Bad Input").build();
			throw new WebApplicationException(response);
		}
		
		String userId = "dummy@gmail.com";
		String[] friends = data.split(",");
		// if friends already have a relation with the user, add them to event.
		// else send invites
		
		for(String friend : friends){
			
		}
		
		return null;
	}
}
