package com.zik.faro.api.activity;

import com.zik.faro.data.Assignment;

import static com.zik.faro.commons.Constants.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENT_PATH_CONST)
public class AssignmentHandler {

    @Path(ASSIGNMENT_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Assignment getAssignment(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                    @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                    @PathParam(ASSIGNMENT_ID_PATH_PARAM) final String assignmentId){
        return null;
    }

}
