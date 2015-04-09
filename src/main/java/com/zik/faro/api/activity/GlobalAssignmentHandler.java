package com.zik.faro.api.activity;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.AssignmentManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
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
    public JResponse<List<Assignment>> getAssignments(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                    @PathParam(EVENT_ID_PATH_PARAM) final String eventId) throws IllegalDataOperation{
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        //TODO: Validate the eventID, userId permissions

        List<Assignment> assignments = AssignmentManagement.getAllAssignments(eventId);
        return JResponse.ok(assignments).build();
    }
}
