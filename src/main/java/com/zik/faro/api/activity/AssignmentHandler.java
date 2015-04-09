package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.*;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Assignment;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENT_PATH_CONST)
public class AssignmentHandler {

    //TODO: add Update/create Assignment API
	
    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Assignment getAssignment(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                    @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                    @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId,
                                    @QueryParam(ACTIVITY_ID_PATH_PARAM) final String activityId) throws DataNotFoundException{
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(assignmentId, "assignmentId");
        
        if(activityId == null || activityId.isEmpty())
        	return AssignmentManagement.getEventLevelAssignment(eventId);
        else
        	return AssignmentManagement.getActivityLevelAssignment(eventId, activityId, assignmentId);
    }

    @Path(ASSIGNMENT_PENDING_COUNT_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public AssignmentCount getPendingAssignmentCount(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                          @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        //TODO: Validate the eventID, userId permissions
        return new AssignmentCount(eventId,AssignmentManagement.getPendingAssignmentCount(eventId));
    }
    
    @Path(ALL)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Assignment> getAssignments(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                    @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
    	ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        
        return AssignmentManagement.getAllAssignments(eventId);
    }

    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String deleteAssignment(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                            @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                            @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(assignmentId, "assignmentId");
        //TODO: Validate the eventID, userId permissions

        return HTTP_OK;
    }
    
    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING+ASSIGNMENT_UPDATE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String updateAssignment(){
    	 return HTTP_OK;
    }
    

    @XmlRootElement
    private static class AssignmentCount{
        public String eventId;      //TODO: Change type to Id;
        public int assignmentCount;
        AssignmentCount(final String eventId, final int assignmentCount){
            this.eventId = eventId;
            this.assignmentCount = assignmentCount;
        }
        AssignmentCount(){}
    }


}
