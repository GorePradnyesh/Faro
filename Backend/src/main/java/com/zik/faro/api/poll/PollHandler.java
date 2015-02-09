package com.zik.faro.api.poll;


import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.Poll;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static com.zik.faro.commons.Constants.*;

@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + POLL_PATH_CONST)
public class PollHandler {
    @Path(POLL_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Poll getPoll(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                        @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                        @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(pollId, "pollId");

        //TODO: Validate the pollId, eventID, userID ACLS
        List<Poll.PollOption> options = new ArrayList<>();
        options.add(new Poll.PollOption("Shasta"));
        Poll.PollOption vegasOption = new Poll.PollOption("Las Vegas");
        vegasOption.addVoters("user1");
        vegasOption.addVoters("user2");
        options.add(vegasOption);
        Poll dummyPoll = new Poll(eventId, "dummyCreator", options, "dummyOwner", "sample skeleton poll");
        dummyPoll.setWinnerId(vegasOption.id);
        return dummyPoll;
    }

    @Path(POLL_CREATE_PATH_CONST)
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String createPoll(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                             @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                             Poll poll){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(poll, "poll");
        //TODO: Validate the poll object make sure the exact set of parameters are set. Not more not less

        return HTTP_OK;
    }

    @Path(POLL_ID_PATH_PARAM_STRING + POLL_UNVOTED_COUNT_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public VoteCount getUnvotedCount(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                                @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                                @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(pollId, "pollId");

        //TODO: Validate the pollId, eventID, userID ACLs
        return new VoteCount(eventId, pollId, 44);
    }


    /*
    Example payload
    <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
    <identifier>
        <idString>3d824de5-98e7-4a4e-93ae-0dc372b11e11</idString>
    </identifier>
     */
    @Path(POLL_ID_PATH_PARAM_STRING + POLL_VOTE_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String castVote(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                          @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                          @PathParam(POLL_ID_PATH_PARAM) final String pollId,
                          Identifier voteId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(pollId, "pollId");
        ParamValidation.genericParamValidations(voteId, "voteId");

        //TODO: Validate the pollId, eventID, userID ACLs
        //TODO: Validate pollOption has only the particular fields set.
        //TODO: Validate existence of poll option specified

        //TODO: Extract the voteId string from the Identifier and then use it.
        return HTTP_OK;
    }

    @Path(POLL_ID_PATH_PARAM_STRING + POLL_CLOSE_PATH_CONST)
    @POST
    public String closePoll(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                           @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                           @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(pollId, "pollId");

        //TODO: Validate the pollId, eventID, userID ACLs
        //TODO: Validate pollOption has only the particular fields set.
        //TODO: Validate existence of poll option specified
        return HTTP_OK;
    }

    @Path(POLL_ID_PATH_PARAM_STRING)
    @DELETE
    public String deletePoll(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature,
                            @PathParam(EVENT_ID_PATH_PARAM) final String eventId,
                            @PathParam(POLL_ID_PATH_PARAM) final String pollId){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(eventId, "eventId");
        ParamValidation.genericParamValidations(pollId, "pollId");

        //TODO: Validate the pollId, eventID, userID ACLs
        //TODO: Validate pollOption has only the particular fields set.
        //TODO: Validate existence of poll option specified
        return HTTP_OK;
    }

    @XmlRootElement
    private static class VoteCount{
        public String eventId;
        public String pollId;
        public Integer pollCount;

        private VoteCount(){}
        VoteCount(final String eventId, final String pollId, final Integer pollCount) {
            this.pollCount = pollCount;
            this.eventId = eventId;
            this.pollId = pollId;
        }
    }

}
