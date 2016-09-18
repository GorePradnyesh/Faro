package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.ACTIVITY_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.ACTIVITY_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.ACTIVITY_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.ACTIVITY_PATH_CONST;
import static com.zik.faro.commons.Constants.ACTIVITY_UPDATE_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.JResponse;
import com.zik.faro.data.Activity;
import com.zik.faro.applogic.ActivityManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITY_PATH_CONST)
public class ActivityHandler {
	@Context
	SecurityContext context;
	
    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Activity> getActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
    	try{
    		Activity activity = ActivityManagement.getActivity(eventId, activityId);
    		return JResponse.ok(activity).build();
    		
        } catch (DataNotFoundException e) {
        	
            Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
        }
    }

    /*
    Something like
    <?xml version="1.0" encoding="UTF-8" ?>
    <activity>
        <eventId>s</eventId>
        <name>hiking</name>
    </activity>
    */
    @POST
    @Path(ACTIVITY_CREATE_PATH_CONST)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Activity> createActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 Activity activity){
    	return JResponse.ok(ActivityManagement.createActivity(activity)).build();
    }

    /*
    Payload looks something like
    <activityUpdateData>
        <description>MyEvent</description>
        <date>
            <date>2014-11-06T11:55:54.297-08:00</date>
            <offset>120000</offset>
        </date>
        <location>
            <locationName>Lake Shasta</locationName>
        </location>
    </activityUpdateData>
    */
    
    @POST
    @Path(ACTIVITY_ID_PATH_PARAM_STRING + ACTIVITY_UPDATE_PATH_CONST)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Activity> updateActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                               @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId,
                               Activity activityUpdateData){
        activityUpdateData.setId(activityId);
        activityUpdateData.setEventId(eventId);
        try {
        	return JResponse.ok(ActivityManagement.updateActivity(activityUpdateData, eventId)).build();
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
		} catch (UpdateVersionException e) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
    }


    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @DELETE
    public JResponse<String> deleteActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
       ActivityManagement.deteleActivity(eventId, activityId);
       return JResponse.ok(Constants.HTTP_OK).build();
    }
    
}
