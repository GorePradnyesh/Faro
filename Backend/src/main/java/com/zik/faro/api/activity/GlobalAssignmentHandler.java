package com.zik.faro.api.activity;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Assignment;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.zik.faro.commons.Constants.*;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENTS_PATH_CONST)
public class GlobalAssignmentHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Map<String,Assignment>> getAssignments(@PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        Map<String,Assignment> assignments = null;
		try {
			assignments = AssignmentManagement.getAllAssignments(eventId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
		return JResponse.ok(assignments).build();
    }
}
