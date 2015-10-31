package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.EventCreateData;
import com.zik.faro.data.Location;

public class FunctionalPollTest {
	private static URL endpoint;
    private static String token = null;
    private static String eventId = null;

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken();
        createEvent();
    }
    
    // Create an event for all activity tests
    private static void createEvent() throws IOException{
    	EventCreateData eventCreateData = new EventCreateData("Dinner", Calendar.getInstance(),
                Calendar.getInstance(), new Location("SFO"), null);
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
    	eventId = event.getEventId();
    }
    
    @SuppressWarnings("deprecation")
	private static void assertEntity(Poll pollRequest, Poll pollResponse){
    	Assert.assertEquals(pollRequest.getCreatorId(), pollResponse.getCreatorId());
    	Assert.assertEquals(pollRequest.getOwner(), pollResponse.getOwner());
    	Assert.assertEquals(pollRequest.getDescription(), pollResponse.getDescription());
    	Assert.assertEquals(pollRequest.getEventId(), pollResponse.getEventId());
    	Assert.assertNotNull(pollResponse.getId());
    	Assert.assertEquals(pollRequest.getWinnerId(), pollResponse.getWinnerId());
    	Assert.assertEquals(pollRequest.getDeadline(), pollResponse.getDeadline());
    	Assert.assertEquals(pollRequest.getStatus(), pollResponse.getStatus());
    	Assert.assertEquals(pollRequest.getPollOptions().size(), pollResponse.getPollOptions().size());
    	for(int i = 0 ; i < pollResponse.getPollOptions().size() ; i++){
    		Assert.assertEquals(pollResponse.getPollOptions().get(i).getOption(), pollRequest.getPollOptions().get(i).getOption());
    		Assert.assertEquals(pollResponse.getPollOptions().get(i).getVoters().size(), pollRequest.getPollOptions().get(i).getVoters().size());
    		Assert.assertEquals(pollResponse.getPollOptions().get(i).getId(),pollRequest.getPollOptions().get(i).getId());
    	}
    }
    
    public static void createAndReadAndDeletePollTest() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", pollOptions, "Kaivan", "Dinner cuisine");
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        // Read recently created poll
        ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/poll/"+pollResponse.getId(), new MultivaluedMapImpl(), token);
        pollResponse = readResponse.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        
        // Delete poll
        ClientResponse deleteResponse = TestHelper.doDELETE(endpoint.toString(), "v1/event/"+eventId+"/poll/"+pollResponse.getId(),token);
        Assert.assertEquals(200, deleteResponse.getStatus());
        Assert.assertEquals("OK", deleteResponse.getEntity(String.class));
        
        // Read recently created poll
        readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/poll/"+pollResponse.getId(), new MultivaluedMapImpl(), token);
        Assert.assertEquals(404, readResponse.getStatus());
        String notFoundResponse = readResponse.getEntity(String.class);
        Assert.assertTrue(notFoundResponse.contains("Data not found"));
    }
     
    public static void getUnvotedCountTest() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", pollOptions, "Kaivan", "Dinner cuisine");
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        
        // Read recently created poll
        ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/poll/"+pollResponse.getId()+"/unvoted/count", new MultivaluedMapImpl(), token);
        Integer count = readResponse.getEntity(Integer.class);
        Assert.assertEquals(1, count.intValue());
    }
    
    public static void castVoteTest() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", pollOptions, "Kaivan", "Dinner cuisine");
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        // Cast vote
        Set<String> voteOption = new HashSet<String>();
        voteOption.add(pollOptions.get(1).getId());
        ClientResponse castVoteResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/vote", token, voteOption);
        String http_ok = castVoteResponse.getEntity(String.class);
        Assert.assertEquals(200,castVoteResponse.getStatus());
        Assert.assertEquals("OK", http_ok);
        
        // Verify vote casted
        ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/poll/"+pollResponse.getId(), new MultivaluedMapImpl(), token);
        pollResponse = readResponse.getEntity(Poll.class);
        Assert.assertEquals(1,pollResponse.getPollOptions().get(1).getVoters().size());
    	
    }
    
    public static void getPolls() throws Exception{
    	ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/polls", new MultivaluedMapImpl(), token);
        List<Poll> polls = readResponse.getEntity(new GenericType<List<Poll>>(){});
        Assert.assertEquals(2, polls.size());
    }
    
    @Test
    public void allTest() throws Exception{
    	createAndReadAndDeletePollTest();
    	getUnvotedCountTest();
    	castVoteTest();
    	getPolls();
    }
}
