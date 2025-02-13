package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.ACTIVITIES_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.JResponse;
import com.zik.faro.data.Activity;
import com.zik.faro.applogic.ActivityManagement;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITIES_PATH_CONST)
public class GlobalActivityHandler {
	
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Activity>> getActivities(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        List<Activity> activityList = ActivityManagement.getActivities(eventId);
        return JResponse.ok(activityList).build();
    }
}
