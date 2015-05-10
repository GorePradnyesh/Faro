package com.zik.faro.api.poll;

import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.POLLS_PATH_CONST;
import static com.zik.faro.commons.Constants.POLL_UNVOTED_COUNT_CONST;
import static com.zik.faro.commons.Constants.SIGNATURE_QUERY_PARAM;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.zik.faro.applogic.PollManagement;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Poll;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + POLLS_PATH_CONST)
public class GlobalPollHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Poll> getPolls(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                        @PathParam(EVENT_ID_PATH_PARAM) final String eventId) {
        ParamValidation.genericParamValidations(eventId, "eventId");

        return PollManagement.getPolls(eventId);
    }
    
    @GET
    @Path(POLL_UNVOTED_COUNT_CONST)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public int getUnvotedCount(@PathParam(EVENT_ID_PATH_PARAM_STRING) final String eventId,
    		@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
    	String userId = signature;
    	return PollManagement.getCountOfUnvotedPolls(userId, eventId);
    }
    
}
