package com.zik.faro.api.activity;

import static com.zik.faro.commons.Constants.*;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Assignment;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + ASSIGNMENTS_PATH_CONST)
public class GlobalAssignmentHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Map<String,Assignment> getAssignments(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                    @PathParam(EVENT_ID_PATH_PARAM) final String eventId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        //TODO: Validate the eventID, userId permissions

        Map<String,Assignment> assignments = null;
		try {
			assignments = AssignmentManagement.getAllAssignments(eventId);
		} catch (DataNotFoundException e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
            throw new WebApplicationException(response);
		}
		return assignments;
    }
}
