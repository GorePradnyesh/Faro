package com.zik.faro.api.event;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.FaroUserName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@Path(Constants.EVENT_ID_PATH_CONST)
public class EventHandler {

    /*@Path(Constants.EVENT_ID_PATH_PARAM_STRING + "/" + Constants.EVENT_DETAILS_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEventDetails(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);

        //TODO: replace the dummy static code below with the actual calls
        Event dummyEvent = new Event("Lake Shasta",
                new DateOffset(new Date(), 60 * 1000),
                new DateOffset(new Date(), 2 * 60* 1000),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));
        return dummyEvent;
    }*/

}
