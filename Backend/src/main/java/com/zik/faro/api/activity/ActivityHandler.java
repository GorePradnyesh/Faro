package com.zik.faro.api.activity;

import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Location;

import static com.zik.faro.commons.Constants.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITY_PATH_CONST)
public class ActivityHandler {

    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Activity getActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
    							@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
    	//TODO: Ensure all event or its children calls are validated with the user being part of event.. Essentially check EventUser relation..
        ParamValidation.validateSignature(signature);
        try{
        	 return ActivityManagement.getActivity(eventId, activityId);
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
    @Path(ACTIVITY_CREATE_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void createActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 Activity activity){
    	
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(activity, "activity");
        //TODO: validate event_id and activity information
        ActivityManagement.createActivity(activity);
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
    
    @Path(ACTIVITY_ID_PATH_PARAM_STRING + ACTIVITY_UPDATE_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void updateActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                               @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                               @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId,
                               ActivityUpdateData activityUpdateData){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(activityUpdateData, "activityUpdateData");
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(activityId, "actvityId");
        //TODO: Validate the eventID and activityID permissions
        Activity activity = new Activity(eventId, null, 
        		activityUpdateData.getDescription(), activityUpdateData.getLocation(), 
        		activityUpdateData.getDate(), null);
        try {
			ActivityManagement.updateActivity(activity, eventId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
        
    }


    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @DELETE
    public void deleteActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(activityId, "activityId");
        //TODO: Validate the eventID and activityID permissions
        ActivityManagement.deteleActivity(eventId, activityId);
    }

    @XmlRootElement
    private static class ActivityUpdateData {
        private String description;
        private Calendar date;
        private Location location;
        
        private ActivityUpdateData(){
        }
        
        public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Calendar getDate() {
			return date;
		}

		public void setDate(Calendar date) {
			this.date = date;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

    }

}
