package com.zik.faro.api.event;


import static com.zik.faro.commons.Constants.EVENT_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.bean.Event;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.applogic.EventManagement;

@Path(EVENT_PATH_CONST + EVENT_CREATE_PATH_CONST)
public class EventCreateHandler {
    @Context
    SecurityContext context;
    //TODO: Get events for a particular USER !!!

    /*
    Accepts something like
    <eventCreateData>
        <eventName>MyEvent</eventName>
        <startDate>
            <date>2014-11-06T11:55:54.297-08:00</date>
            <offset>60000</offset>
        </startDate>
        <endDate>
            <date>2014-11-06T11:55:54.297-08:00</date>
            <offset>120000</offset>
        </endDate>
        <location>
            <locationName>Lake Shasta</locationName>
        </location>
    </eventCreateData>
    */

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Event> createEvent(EventCreateData eventCreateData){
        final String userId = context.getUserPrincipal().getName();

        Event event = EventManagement.createEvent(userId, eventCreateData);
        return JResponse.ok(event).build();
    }

}
