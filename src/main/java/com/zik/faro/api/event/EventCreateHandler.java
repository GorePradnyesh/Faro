package com.zik.faro.api.event;


import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Event;
import com.zik.faro.persistence.datastore.EventDatastoreImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import static com.zik.faro.commons.Constants.*;



@Path(EVENT_PATH_CONST + EVENT_CREATE_PATH_CONST)
public class EventCreateHandler {
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
    public EventManagement.MinEvent createEvent(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                EventCreateData eventCreateData){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventCreateData,"eventCreateData");

        //TODO: Extract userID from Signature
        final String userId = "dummyUser";

        EventManagement.MinEvent minEvent = EventManagement.createEvent(userId, eventCreateData);
        return minEvent;
    }

}
