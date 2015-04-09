package com.zik.faro.api.activity;

import com.zik.faro.applogic.ActivityManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.DateOffset;
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
    	
        ParamValidation.validateSignature(signature);
        return ActivityManagement.getActivity(eventId, activityId);
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
    public String createActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 Activity activity){
    	
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(activity, "activity");
        //TODO: validate event_id and activity information
        ActivityManagement.createActivity(activity);
        return HTTP_OK;
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
    public String updateActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                               @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                               @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId,
                               Activity activityUpdateData){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(activityUpdateData, "activityUpdateData");
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(activityId, "actvityId");
        //TODO: Validate the eventID and activityID permissions
        //ActivityManagement.updateActivity(activityUpdateData, eventId);
        return HTTP_OK;
    }


    @Path(ACTIVITY_ID_PATH_PARAM_STRING)
    @DELETE
    public String deleteActivity(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                 @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                 @PathParam(ACTIVITY_ID_PATH_PARAM) final String activityId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(activityId, "activityId");
        //TODO: Validate the eventID and activityID permissions
        ActivityManagement.deteleActivity(eventId, activityId);
        // TODO: Response template?
        return HTTP_OK;
    }



}
