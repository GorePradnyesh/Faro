package com.zik.faro.api.activity;

import com.sun.jersey.api.JResponse;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Activity;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.zik.faro.commons.Constants.*;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITIES_PATH_CONST)
public class GlobalActivityHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Activity>> getActivities(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.genericParamValidations(eventId, "eventId");

        //TODO: replace the dummy static code below with the actual calls
        List<Activity> activityList = new ArrayList<>();
        activityList.add(new Activity(eventId, "hiking"));
        activityList.add(new Activity(eventId, "swimming"));
        activityList.add(new Activity(eventId, "walking"));

        return JResponse.ok(activityList).build();
    }



}
