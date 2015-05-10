package com.zik.faro.api.event;

import static com.zik.faro.commons.Constants.COUNT_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ADD_FRIENDS_CONST;
import static com.zik.faro.commons.Constants.EVENT_DETAILS_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_DISABLE_CONTROL_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_INVITEES_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_REMOVE_ATTENDEE_PATH_CONST;
import static com.zik.faro.commons.Constants.FARO_USER_ID_PARAM;
import static com.zik.faro.commons.Constants.SIGNATURE_QUERY_PARAM;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.applogic.EventUserManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.Event;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class EventHandler {
	@Context
	SecurityContext context;
	
	@Path(EVENT_DETAILS_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEventDetails(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        //TODO: validate eventIDs

        String userId = "userIdExtractedFromSignature";
        Event retrievedEvent;
		try {
			retrievedEvent = EventManagement.getEventDetails(userId, eventId);
			
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
        return retrievedEvent;
    }

    @Path(EVENT_INVITEES_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public InviteeList getEventInvitees(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                            @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                            @QueryParam(COUNT_PARAM) final int count){
        //TODO: validate eventIDs

        //TODO: replace the dummy static code below with the actual calls
        return EventUserManagement.getEventInvitees(eventId);
    }

    @Path(EVENT_DISABLE_CONTROL_PATH_CONST)
    @POST
    public void disableEventControl(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        //TODO: validate eventIDs
    	String userId = context.getUserPrincipal().getName();
        try {
            EventManagement.disableEventControls(userId, eventId);
        } catch (DataNotFoundException e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        } catch (DatastoreException e) {
        	Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(e.getMessage())
                     .build();
            throw new WebApplicationException(response);
		}
    }

    @Path(EVENT_REMOVE_ATTENDEE_PATH_CONST)
    @POST
    public void removeAttendee(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    							 @QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @QueryParam(FARO_USER_ID_PARAM) final String userId){
        ParamValidation.genericParamValidations(userId, "userId");
        EventUserManagement.removeEventUser(eventId, userId);
    }
    
    @Path(EVENT_ADD_FRIENDS_CONST)
    @POST
    public void addFriend(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
				    		@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
				            @QueryParam(FARO_USER_ID_PARAM) final String userId,
    						final AddFriendRequest data){
    	ParamValidation.genericParamValidations(userId, "userId");
        
        try {
			EventManagement.addFriendToEvent(eventId, userId, data);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		} catch (IllegalDataOperation e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
    }
   
}
