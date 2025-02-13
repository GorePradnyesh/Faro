package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.ACTIVITY_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.ALL;
import static com.zik.faro.commons.Constants.ASSIGNMENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.ASSIGNMENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.ASSIGNMENT_PATH_CONST;
import static com.zik.faro.commons.Constants.ASSIGNMENT_PENDING_COUNT_PATH_CONST;
import static com.zik.faro.commons.Constants.ASSIGNMENT_UPDATE_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;

import java.util.Map;

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

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.data.UpdateCollectionRequest;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENT_PATH_CONST)
public class AssignmentHandler {
	@Context
	SecurityContext context;
	
    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Assignment> getAssignment(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                    @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId,
                                    // No seperate API for event or activity's assignment. 
                                    // If activity passed, assignment of that activity is returned else event assignment returned.
                                    @QueryParam(ACTIVITY_ID_PATH_PARAM) final String activityId) throws DataNotFoundException{
        Assignment assignment = null;
    	if(activityId == null || activityId.isEmpty())
        	assignment = AssignmentManagement.getEventLevelAssignment(eventId);
        else
        	assignment = AssignmentManagement.getActivityLevelAssignment(eventId, activityId, assignmentId);
    	return JResponse.ok(assignment).build();
    }

    @Path(ASSIGNMENT_PENDING_COUNT_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<AssignmentCount> getPendingAssignmentCount(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        try {
			AssignmentCount assignmentCount =  new AssignmentCount(eventId,
					AssignmentManagement.getPendingAssignmentCount(eventId));
			return JResponse.ok(assignmentCount).build();
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
    }
    
    @Path(ALL)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Map<String,Assignment>> getAssignments(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
    	try {
			Map<String,Assignment> assignments = AssignmentManagement.
					getAllAssignments(eventId);
			return JResponse.ok(assignments).build();
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
    }
    
    // TODO: Eventually should be moved to EventHandler
    @Path(ASSIGNMENT_UPDATE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Event> updateEventAssignmentItems(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
    		final UpdateCollectionRequest<Event, Item> updateObj){
    	Event updatedEvent = updateObj.getUpdate();
    	
		try {
			Event updatedEventResponse = AssignmentManagement.updateEventItems(updatedEvent.getId(), updateObj.getToBeAdded(), 
					updateObj.getToBeRemoved(), updatedEvent.getVersion());
			return JResponse.ok(updatedEventResponse).status(Response.Status.OK).build();
		} catch (DataNotFoundException e) {
        	throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Data Not Found");
        } catch (DatastoreException e) {
        	throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Datastore Exception");
		} catch (UpdateVersionException e) {
			throw new FaroWebAppException(FaroResponseStatus.UPDATE_VERSION_MISMATCH, "Update Version Mismatch");
		} catch (UpdateException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Update Error");
		}
	}
    

    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<String> deleteAssignment(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                   @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId){
        // TODO: Dont have clarity. Skipping for now.
    	return JResponse.ok(Constants.HTTP_OK).build();
    }
    

    
    @XmlRootElement
    private static class AssignmentCount{
        public String eventId;      
        public int assignmentCount;
        AssignmentCount(final String eventId, final int assignmentCount){
            this.eventId = eventId;
            this.assignmentCount = assignmentCount;
        }
        AssignmentCount(){}
    }


}
