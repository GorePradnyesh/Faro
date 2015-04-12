package com.zik.faro.api.activity;

import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;

import static com.zik.faro.commons.Constants.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENT_PATH_CONST)
public class AssignmentHandler {

    //TODO: add Update/create Assignment API

    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Assignment getAssignment(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                    @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId){
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(assignmentId, "assignmentId");
        //TODO: Validate the eventID, userId permissions

        Assignment tempAssignment = new Assignment();
        tempAssignment.addItem(new Item("blankets", "David", 4, Unit.COUNT));
        tempAssignment.addItem(new Item("rice", "Roger", 10, Unit.LB));
        return tempAssignment;
    }

    @Path(ASSIGNMENT_PENDING_COUNT_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public AssignmentCount getPendingAssignmentCount(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.genericParamValidations(eventId, "eventId");
        //TODO: Validate the eventID, userId permissions

        return new AssignmentCount(eventId, 44);
    }

    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String deleteAssignment(@PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                   @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId){
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(assignmentId, "assignmentId");
        //TODO: Validate the eventID, userId permissions

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
