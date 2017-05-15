
package com.zik.faro.functional;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;

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
		GeoPosition geoPosition = new GeoPosition(0,0);
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new
				Location("SFO", "CA", geoPosition), "Mafia god");
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
    	eventId = event.getId();
    }
    
    @SuppressWarnings("deprecation")
	private static void assertEntity(Poll pollRequest, Poll pollResponse){
    	Assert.assertEquals(pollRequest.getCreatorId(), pollResponse.getCreatorId());
    	Assert.assertEquals(pollRequest.getOwner(), pollResponse.getOwner());
    	Assert.assertEquals(pollRequest.getDescription(), pollResponse.getDescription());
    	Assert.assertEquals(pollRequest.getEventId(), pollResponse.getEventId());
//    	Assert.assertEquals(pollRequest.getId(), pollResponse.getId());
    	Assert.assertNotNull(pollResponse.getId());
    	Assert.assertEquals(pollRequest.getWinnerId(), pollResponse.getWinnerId());
    	Assert.assertEquals(pollRequest.getDeadline(), pollResponse.getDeadline());
    	Assert.assertEquals(pollRequest.getStatus(), pollResponse.getStatus());
    	Assert.assertEquals(pollRequest.getPollOptions().size(), pollResponse.getPollOptions().size());
    	for(int i = 0 ; i < pollResponse.getPollOptions().size() ; i++){
    		Assert.assertEquals(pollResponse.getPollOptions().get(i).getOption(), pollRequest.getPollOptions().get(i).getOption());
    		Assert.assertNotNull(pollResponse.getPollOptions().get(i).getId());
    		Assert.assertEquals(pollResponse.getPollOptions().get(i).getVoters().size(), pollRequest.getPollOptions().get(i).getVoters().size());
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
   
    
    public static void updatePollTest() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", true, pollOptions, "Kaivan", "Dinner cuisine", ObjectStatus.OPEN);
    	//new Poll(eventId, creator, multiChoice, pollOptions, owner, description, status)
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        // Add poll options
        List<PollOption> newOptions = new ArrayList<PollOption>();
        newOptions.add(new PollOption("Chinese"));
        Poll update = new Poll();
        update.setPollOptions(newOptions);
        update.setEventId(eventId);
        update.setId(pollResponse.getId());
        update.setVersion(pollResponse.getVersion());
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("poll", update);
        ClientResponse updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePoll", token, map);
        Poll updatedPollResponse = updateResponse.getEntity(Poll.class);
        Assert.assertEquals(200, updateResponse.getStatus());
        Assert.assertEquals(4,updatedPollResponse.getPollOptions().size());
    	for(int i = 0; i < updatedPollResponse.getPollOptions().size(); i++){
    		Assert.assertNotNull(updatedPollResponse.getPollOptions().get(i).getId());
    	}
    	
    	// Update Poll properties
    	update = new Poll(); update.setEventId(eventId); update.setId(pollResponse.getId());
    	update.setDescription("Poll Description updated");
    	update.setVersion(updatedPollResponse.getVersion());
    	map.put("poll", update);
    	updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePoll", token, map);
        updatedPollResponse = updateResponse.getEntity(Poll.class);
        Assert.assertEquals(update.getDescription(), updatedPollResponse.getDescription());
        Long version = update.getVersion();
        Assert.assertEquals(++version, updatedPollResponse.getVersion());
        
        // Update Poll properties and cast vote
        
        update = new Poll(); update.setEventId(eventId); update.setId(pollResponse.getId());
    	update.setDescription("Poll Description updated again");
    	update.setVersion(updatedPollResponse.getVersion());
    	map.put("poll", update);
    	Set<String> voteOption = new HashSet<String>();
        voteOption.add(pollResponse.getPollOptions().get(1).getId());
        map.put("voteOption", voteOption);
    	updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePoll", token, map);
        updatedPollResponse = updateResponse.getEntity(Poll.class);
        Assert.assertEquals(update.getDescription(), updatedPollResponse.getDescription());
        version = update.getVersion();
        Assert.assertEquals(++version, updatedPollResponse.getVersion());
        Assert.assertEquals(1,updatedPollResponse.getPollOptions().get(1).getVoters().size());
    }
    
    public static void testTest() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", true, pollOptions, "Kaivan", "Dinner cuisine", ObjectStatus.OPEN);
    	//new Poll(eventId, creator, multiChoice, pollOptions, owner, description, status)
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        // Add poll options
        // Cast vote
        Set<String> voteOption = new HashSet<String>();
        voteOption.add(pollResponse.getPollOptions().get(1).getId());
        voteOption.add(pollResponse.getPollOptions().get(2).getId());
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("version", pollResponse.getVersion());
        map.put("voteOption", voteOption);
        map.put("poll", p);
        ClientResponse updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/test", token, map);
        Poll updatedPollResponse = updateResponse.getEntity(Poll.class);
        Assert.assertEquals(200, updateResponse.getStatus());
        Assert.assertEquals(4,updatedPollResponse.getPollOptions().size());
    	for(int i = 0; i < updatedPollResponse.getPollOptions().size(); i++){
    		Assert.assertNotNull(updatedPollResponse.getPollOptions().get(i).getId());
    	}
    }
    
    public static void getPolls() throws Exception{
    	ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/polls", new MultivaluedMapImpl(), token);
        List<Poll> polls = readResponse.getEntity(new GenericType<List<Poll>>(){});
        Assert.assertEquals(1, polls.size());
    }
    
    @Test
    public void allTest() throws Exception{
    	createAndReadAndDeletePollTest();
    	getUnvotedCountTest();
    	getPolls();
    	updatePollTest();
    }
}
