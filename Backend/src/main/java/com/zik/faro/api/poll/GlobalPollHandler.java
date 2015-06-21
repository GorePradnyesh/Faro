package com.zik.faro.api.poll;

import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.POLLS_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_UNVOTED_COUNT_CONST;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.PollManagement;
import com.zik.faro.data.Poll;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + POLLS_PATH_CONST)
public class GlobalPollHandler {
    @Context
    SecurityContext context;
	
	@GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Poll>> getPolls(@PathParam(EVENT_ID_PATH_PARAM) final String eventId) {
        return JResponse.ok(PollManagement.getPolls(eventId)).build();
    }
    
    @GET
    @Path(POLL_UNVOTED_COUNT_CONST)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Integer> getUnvotedCount(@PathParam(EVENT_ID_PATH_PARAM_STRING) final String eventId){
    	String userId = context.getUserPrincipal().getName();
    	return JResponse.ok(PollManagement.getCountOfUnvotedPolls(userId, eventId))
    			.build();
    }
    
}
