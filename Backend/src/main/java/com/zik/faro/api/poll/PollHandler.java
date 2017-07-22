package com.zik.faro.api.poll;


import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.HTTP_OK;
import static com.zik.faro.commons.Constants.POLL_CLOSE_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.POLL_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.POLL_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_UNVOTED_COUNT_CONST;
import static com.zik.faro.commons.Constants.POLL_UPDATE_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_VOTE_PATH_CONST;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.PollManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Poll;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + POLL_PATH_CONST)
public class PollHandler {
    @Context
    SecurityContext context;
    
    private static ObjectMapper mapper = new ObjectMapper();
	
	@Path(POLL_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Poll> getPoll(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                        @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        try {
			return JResponse.ok(PollManagement.getPoll(eventId, pollId)).build();
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
    }

    @Path(POLL_CREATE_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Poll> createPoll(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                             Poll poll){
        String userId = context.getUserPrincipal().getName();
    	try {
        	 return JResponse.ok(PollManagement.createPoll(poll, userId)).build();
		} catch (DataNotFoundException e) {
			// If event not present exception thrown.
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
    }

    @Path(POLL_ID_PATH_PARAM_STRING + POLL_UNVOTED_COUNT_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Integer> getUnvotedCount(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        String userId = context.getUserPrincipal().getName();
        return JResponse.ok(PollManagement.getCountOfUnvotedPolls(userId, eventId))
        		.build();
    }

    
    @Path(POLL_ID_PATH_PARAM_STRING + POLL_UPDATE_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Poll> updatePoll(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                          @PathParam(POLL_ID_PATH_PARAM) final String pollId,
                          // Ids of poll options
                          Map<String,Object> updateObj){
        String userId = context.getUserPrincipal().getName();
        Poll updatedPoll;
        try {
			updatedPoll = PollManagement.update(eventId, pollId, updateObj, userId);
		} catch (DatastoreException e) {
			Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		} catch (UpdateVersionException e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
           throw new WebApplicationException(response);
		}
        return JResponse.ok(updatedPoll).build();
    }

    @Path(POLL_ID_PATH_PARAM_STRING + POLL_CLOSE_PATH_CONST)
    @POST
    public JResponse<String> closePoll(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                            @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        // TODO: Still to implement.. Do not have a very clear view of how this 
        // feature works.
        
        return JResponse.ok(Constants.HTTP_OK).build();
    }
   

    @Path(POLL_ID_PATH_PARAM_STRING)
    @DELETE
    public JResponse<String> deletePoll(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                             @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        String userId = context.getUserPrincipal().getName();
    	try {
			PollManagement.deletePoll(eventId, pollId, userId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
        return JResponse.ok(Constants.HTTP_OK).build();
    }
}
