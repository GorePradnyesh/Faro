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
import static com.zik.faro.commons.Constants.UPDATE_INVITE_STATUS_PATH_CONST;

import javax.ws.rs.DELETE;
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

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.applogic.EventUserManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.Event;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class EventHandler {
	@Context
	SecurityContext context;
	
	@Path(EVENT_DETAILS_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Event> getEventDetails(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        String userId = context.getUserPrincipal().getName();
        Event retrievedEvent;
		try {
			retrievedEvent = EventManagement.getEventDetails(userId, eventId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(e.getMessage())
					.build();
            throw new WebApplicationException(response);
		}
        return JResponse.ok(retrievedEvent).build();
    }

    @Path(EVENT_INVITEES_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<InviteeList> getEventInvitees(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                            @QueryParam(COUNT_PARAM) final int count){
    	// TODO: Implement count.
        InviteeList list = EventUserManagement.getEventInvitees(eventId);
        return JResponse.ok(list).build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<String> deleteEvent(@PathParam(EVENT_ID_PATH_PARAM) final String eventId) {
        EventManagement.deleteEvent(eventId);
        EventUserManagement.deleteRelationForEvent(eventId);
        return JResponse.ok(Constants.HTTP_OK).build();
    }
    
    @Path(UPDATE_INVITE_STATUS_PATH_CONST)
    @POST
    public JResponse<String> updateInviteStatus(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    		final EventInviteStatus inviteStatus){
    	String userId = context.getUserPrincipal().getName();
    	try{
    		EventUserManagement.updateEventUserInviteStatus(eventId, userId, inviteStatus);
    	}catch (DataNotFoundException e) {
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
    	return JResponse.ok(Constants.HTTP_OK).build();
    }

    @Path(EVENT_DISABLE_CONTROL_PATH_CONST)
    @POST
    public JResponse<String> disableEventControl(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
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
        return JResponse.ok(Constants.HTTP_OK).build();
    }

    @Path(EVENT_REMOVE_ATTENDEE_PATH_CONST)
    @POST
    public JResponse<String> removeAttendee(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    		final String attendeeToBeRemoved){
    	String userId = context.getUserPrincipal().getName();
        EventUserManagement.removeEventUser(eventId, attendeeToBeRemoved);
        return JResponse.ok(Constants.HTTP_OK).build();
    }
    
    @Path(EVENT_ADD_FRIENDS_CONST)
    @POST
    public JResponse<String> addFriend(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
				    		final AddFriendRequest data){
    	String userId = context.getUserPrincipal().getName();
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
        return JResponse.ok(Constants.HTTP_OK).build();
    }
   
}
