package com.zik.faro.api.poll;

import com.sun.jersey.api.JResponse;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.Poll;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.zik.faro.commons.Constants.*;


@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + POLLS_PATH_CONST)
public class GlobalPollHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<Poll>> getPolls(@PathParam(EVENT_ID_PATH_PARAM) final String eventId) {
        ParamValidation.genericParamValidations(eventId, "eventId");

        List<Poll> polls = new ArrayList<>();

        List<Poll.PollOption> options = new ArrayList<>();
        options.add(new Poll.PollOption("Shasta"));
        Poll.PollOption vegasOption = new Poll.PollOption("Las Vegas");
        vegasOption.addVoters("user1");
        vegasOption.addVoters("user2");
        options.add(vegasOption);
        Poll dummyPoll = new Poll(eventId, "dummyCreator", options, "dummyOwner", "sample skeleton poll");
        dummyPoll.setWinnerId(vegasOption.id);


        List<Poll.PollOption> options2 = new ArrayList<>();
        options2.add(new Poll.PollOption("Barbeque"));
        Poll.PollOption foodOption1 = new Poll.PollOption("Indian Food");
        foodOption1.addVoters("user1");
        foodOption1.addVoters("user2");
        options2.add(foodOption1);
        Poll dummyPoll2 = new Poll(eventId, "dummyCreator", options2, "dummyOwner", "sample skeleton poll");
        dummyPoll2.setWinnerId(foodOption1.id);


        polls.add(dummyPoll);
        polls.add(dummyPoll);

        //TODO: Validate the eventID, userID ACLS
        return JResponse.ok(polls).build();
    }
}
