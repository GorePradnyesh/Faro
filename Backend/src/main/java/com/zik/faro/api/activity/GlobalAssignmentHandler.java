package com.zik.faro.api.activity;

import com.sun.jersey.api.JResponse;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.data.Unit;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.zik.faro.commons.Constants.*;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENTS_PATH_CONST)
public class GlobalAssignmentHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Assignment>> getAssignments(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.genericParamValidations(eventId, "eventId");
        //TODO: Validate the eventID, userId permissions

        List<Assignment> assignments = new ArrayList<>();
        Assignment tempAssignment = new Assignment();
        tempAssignment.addItem(new Item("blankets", "David", 4, Unit.COUNT));
        tempAssignment.addItem(new Item("rice", "Roger", 10, Unit.LB));

        Assignment assignment2 = new Assignment();
        assignment2.addItem(new Item("rope", "David", 10, Unit.METER));

        assignments.add(tempAssignment);
        assignments.add(assignment2);
        return JResponse.ok(assignments).build();
    }
}
