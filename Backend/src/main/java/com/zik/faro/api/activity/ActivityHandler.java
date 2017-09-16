package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.ActivityManagement;
import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponse;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITY_PATH_CONST)
public class ActivityHandler {
	@Context
	SecurityContext context;
	
	private static Logger logger = LoggerFactory.getLogger(ActivityHandler.class);
	
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
    	String userId = context.getUserPrincipal().getName();
    	return JResponse.ok(ActivityManagement.createActivity(activity, userId)).build();
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
    public JResponse<FaroResponse<Activity>> updateActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                               @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId,
                               UpdateRequest<Activity> updateObj){
    	String userId = context.getUserPrincipal().getName();
    	Set<String> updatedFields = updateObj.getUpdatedFields();
    	Activity updatedActivity = updateObj.getUpdate();
    	updatedActivity.setId(activityId);
    	updatedActivity.setEventId(eventId);
        try {
        	updatedActivity = ActivityManagement.updateActivity(updatedActivity, eventId, userId, updatedFields);
        	FaroResponseStatus updateStatus = FaroResponseStatus.OK;
        	FaroResponse<Activity> response = new FaroResponse<Activity>(updatedActivity, updateStatus);
        	return JResponse.ok(response).status(response.getFaroResponseStatus().getRestResponseStatus()).build();
		} catch (DataNotFoundException e) {
        	throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND);
        } catch (DatastoreException e) {
        	throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, e.getMessage());
		} catch (UpdateVersionException e) {
			throw new FaroWebAppException(FaroResponseStatus.UPDATE_VERSION_MISMATCH);
		} catch (UpdateException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Update Error");
		}
    }
    
    @Path(ACTIVITY_ID_PATH_PARAM_STRING + ASSIGNMENT_PATH_CONST + ASSIGNMENT_UPDATE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<FaroResponse<Activity>> updateActivityAssignmentItems(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    		@PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId, final UpdateCollectionRequest<Activity, Item> updateObj){
    	String userId = context.getUserPrincipal().getName();
    	Activity updatedActivity = updateObj.getUpdate();
    	updatedActivity.setEventId(eventId);
    	updatedActivity.setId(activityId);
    	try {
			Activity updatedEventResponse = AssignmentManagement.updateActivityItems2(updatedActivity.getEventId(), updatedActivity.getId(), updateObj.getToBeAdded(), 
					updateObj.getToBeRemoved(), updatedActivity.getVersion());
			FaroResponseStatus updateStatus = FaroResponseStatus.OK;
        	FaroResponse<Activity> response = new FaroResponse<Activity>(updatedEventResponse, updateStatus);
        	return JResponse.ok(response).status(response.getFaroResponseStatus().getRestResponseStatus()).build();
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
		} catch (UpdateException e) {
			Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
	}


    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @DELETE
    public JResponse<String> deleteActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
    	String userId = context.getUserPrincipal().getName();
    	try{
    		ActivityManagement.deleteActivity(eventId, activityId, userId);
    		return JResponse.ok(Constants.HTTP_OK).build();
    	} catch (DataNotFoundException e) {
       	
    		Response response = Response.status(Response.Status.NOT_FOUND)
                   .entity(e.getMessage())
                   .build();
    		throw new WebApplicationException(response);
       }
       
    }
    
}
