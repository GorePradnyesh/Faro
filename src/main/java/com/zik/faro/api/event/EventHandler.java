package com.zik.faro.api.event;

import static com.zik.faro.commons.Constants.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.applogic.EventUserManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class EventHandler {
	
	@Path(EVENT_DETAILS_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEventDetails(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        //TODO: validate eventIDs

        String userId = "userIdExtractedFromSignature";
        Event retrievedEvent = EventManagement.getEventDetails(userId, eventId);
        return retrievedEvent;
    }

    //TODO: Add maxCount query idString param;
    @Path(EVENT_INVITEES_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public InviteeList getEventInvitees(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                            @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        //TODO: validate eventIDs

        //TODO: replace the dummy static code below with the actual calls
        return EventUserManagement.getEventInvitees(eventId);
    }

    @Path(EVENT_DISABLE_CONTROL_PATH_CONST)
    @POST
    public String disableEventControl(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                      @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        //TODO: validate eventIDs

        String userId = "userIdExtractedFromSignature";        try {
            EventManagement.disableEventControls(userId, eventId);
        } catch (DataNotFoundException e) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        }
        return HTTP_OK;
    }

    @Path(EVENT_REMOVE_ATTENDEE_PATH_CONST)
    @POST
    public String removeAttendee(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    							 @QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @QueryParam(FARO_USER_ID_PARAM) final String userId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");
        EventUserManagement.removeEventUser(eventId, userId);
        // TODO: What should be returned here?
        return HTTP_OK;
    }
    
    @Path(EVENT_ADD_FRIENDS_CONST)
    @POST
    public String addFriend(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
				    		@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
				            @QueryParam(FARO_USER_ID_PARAM) final String userId,
    						final AddFriendRequest data){
    	ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");
        
        EventManagement.addFriendToEvent(eventId, userId, data);
        
    	return null;
    }
   
}
