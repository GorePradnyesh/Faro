package com.zik.faro.api.event;

import static com.zik.faro.commons.Constants.EVENTS_PATH_CONST;
import static com.zik.faro.commons.Constants.SIGNATURE_QUERY_PARAM;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.data.Event;

@Path(EVENTS_PATH_CONST)
public class GlobalEventHandler {
    
	@Context
	SecurityContext context;
	
	@GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Event>> getEvents(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
    	String userId = context.getUserPrincipal().getName();
        List<Event> eventList = EventManagement.getEvents(userId);
        return JResponse.ok(eventList).build();
    }
  
}
