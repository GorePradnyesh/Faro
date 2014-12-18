package com.zik.faro.api.event;

import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.api.responder.MinUser;
import static com.zik.faro.commons.Constants.*;

import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.InviteStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class EventHandler {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEventDetails(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        //TODO: validate eventIDs

        //TODO: replace the dummy static code below with the actual calls
        Event dummyEvent = new Event("Lake Shasta "+ eventId,
                new DateOffset(new Date(), 60 * 1000),
                new DateOffset(new Date(), 2 * 60* 1000),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));
        return dummyEvent;
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
        InviteeList userStatus = new InviteeList(eventId);
        userStatus.addUserStatus(new MinUser("David", "Gilmour", "dg@gmail.com"), InviteStatus.ACCEPTED);
        userStatus.addUserStatus(new MinUser("Roger", "Waters", "rw@gmail.com"), InviteStatus.INVITED);
        return userStatus;
    }

    @Path(EVENT_DISABLE_CONTROL_PATH_CONST)
    @POST
    public String disableEventControl(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                      @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        //TODO: validate eventIDs

        //TODO: replace the dummy static code below with the actual calls
        return HTTP_OK;
    }

    @Path(EVENT_REMOVE_ATTENDEE_PATH_CONST)
    @POST
    public String removeAttendee(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @QueryParam(FARO_USER_ID_PARAM) final String userId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(userId, "userId");
        //TODO: replace the dummy static code below with the actual calls
        return HTTP_OK;
    }

}
