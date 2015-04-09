package com.zik.faro.api.event;

import static com.zik.faro.commons.Constants.EVENTS_PATH_CONST;
import static com.zik.faro.commons.Constants.SIGNATURE_QUERY_PARAM;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

@Path(EVENTS_PATH_CONST)
public class GlobalEventHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Event>> getEvents(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);
        //TODO: Extract userId from signature
        List<Event> eventList = EventManagement.getEvents(/*userId*/"");
        return JResponse.ok(eventList).build();
    }
    
    public static void main(String[] args){
    	List<String> list = new ArrayList<String>();
    	list.add("Kaivan");
    	
    }
}
