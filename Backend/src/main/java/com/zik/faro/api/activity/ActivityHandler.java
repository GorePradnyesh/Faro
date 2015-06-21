package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.ACTIVITY_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.ACTIVITY_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.ACTIVITY_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.ACTIVITY_PATH_CONST;
import static com.zik.faro.commons.Constants.ACTIVITY_UPDATE_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.SIGNATURE_QUERY_PARAM;

import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.applogic.ActivityManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Location;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ACTIVITY_PATH_CONST)
public class ActivityHandler {
	@Context
	SecurityContext context;
	
    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Activity getActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
    							@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
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
    public void updateActivity(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                               @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId,
                               ActivityUpdateData activityUpdateData){
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
