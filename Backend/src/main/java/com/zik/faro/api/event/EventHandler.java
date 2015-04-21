package com.zik.faro.api.event;

import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.api.responder.MinUser;
import static com.zik.faro.commons.Constants.*;

import com.zik.faro.applogic.EventManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.InviteStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class EventHandler {
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEventDetails(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        //TODO: validate eventIDs

        String userId = securityContext.getUserPrincipal().getName();
        Event retrievedEvent = EventManagement.getEventDetails(userId, eventId);
        return retrievedEvent;
    }

    //TODO: Add maxCount query idString param;
    @Path(EVENT_INVITEES_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public InviteeList getEventInvitees(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        //TODO: validate eventIDs

        //TODO: replace the dummy static code below with the actual calls
        InviteeList userStatus = new InviteeList(eventId);
        userStatus.addUserStatus(new MinUser("David", "Gilmour", "dg@gmail.com"), InviteStatus.ACCEPTED);
        userStatus.addUserStatus(new MinUser("Roger", "Waters", "rw@gmail.com"), InviteStatus.INVITED);
        return userStatus;
    }

    @Path(EVENT_DISABLE_CONTROL_PATH_CONST)
    @POST
    public String disableEventControl(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        //TODO: validate eventIDs

        String userId = securityContext.getUserPrincipal().getName();
        try {
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
    public String removeAttendee(@QueryParam(FARO_USER_ID_PARAM) final String userId){
        ParamValidation.genericParamValidations(userId, "userId");
        //TODO: replace the dummy static code below with the actual calls
        return HTTP_OK;
    }

}
