
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
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.commons.FaroResponse;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;

public class FunctionalPollTest {
	private static URL endpoint;
    private static String token = null;
    private static String eventId = null;
    private static String callerEmail = UUID.randomUUID().toString() + "@gmail.com";

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken(callerEmail);
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
   
    @Test
    public void updatePollTest() throws Exception{
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
        
        UpdateRequest<Poll> updateObj = new UpdateRequest<>();
        pollResponse.setCreatorId("Jawahar");
        Calendar newDeadline = Calendar.getInstance();
        pollResponse.setDeadline(newDeadline);
        pollResponse.setDescription("Updated description");
        updateObj.setUpdate(pollResponse);
        Set<String> fields = new HashSet<String>();
        fields.add("creatorId");fields.add("description");fields.add("deadline");
        updateObj.setUpdatedFields(fields);
        ClientResponse updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePoll", token, updateObj);
        FaroResponse<Poll> faroResponse = updateResponse.getEntity(new GenericType<FaroResponse<Poll>>(){});
        
        Poll updatedPollResponse= faroResponse.getEntity();
        Assert.assertEquals("Updated description",updatedPollResponse.getDescription());
        Assert.assertEquals("Jawahar",updatedPollResponse.getCreatorId());
        Assert.assertEquals(newDeadline.getTimeInMillis(),updatedPollResponse.getDeadline().getTimeInMillis());
    }
    
    @Test
    public void updatePollOptions() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", pollOptions, "Kaivan", "Dinner cuisine");
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        
        // Update: Add and Remove
        List<PollOption> add = new ArrayList<>();
        add.add(new PollOption("Venezuaelan"));
        List<PollOption> remove = new ArrayList<>();
        remove.add(pollResponse.getPollOptions().get(1));
        UpdateCollectionRequest<Poll, PollOption> updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeAdded(add);
        updateRequest.setToBeRemoved(remove);
        updateRequest.setUpdate(pollResponse);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePollOptions", token, updateRequest);
        FaroResponse<Poll> faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll= faroResponse.getEntity();
        
        Assert.assertEquals(updateResponsePoll.getPollOptions().size(), 3);
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(0).getOption(), pollOptions.get(0).getOption());
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(1).getOption(), pollOptions.get(2).getOption());
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(2).getOption(), add.get(0).getOption());
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(0).getId(), pollResponse.getPollOptions().get(0).getId());
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(1).getId(), pollResponse.getPollOptions().get(2).getId());
        
        // Update: Only Remove
        remove.clear();
        remove.add(updateResponsePoll.getPollOptions().get(2));
        updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeRemoved(remove);
        updateRequest.setUpdate(updateResponsePoll);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePollOptions", token, updateRequest);
        faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll1= faroResponse.getEntity();
        Assert.assertEquals(updateResponsePoll1.getPollOptions().size(), 2);
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(0).getOption(), updateResponsePoll.getPollOptions().get(0).getOption());
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(1).getOption(), updateResponsePoll.getPollOptions().get(1).getOption());
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(0).getId(), updateResponsePoll.getPollOptions().get(0).getId());
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(1).getId(), updateResponsePoll.getPollOptions().get(1).getId());
        
        // Update: Only Add
        add.clear();
        add.add(new PollOption("Pakistani"));
        updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeAdded(add);
        updateRequest.setUpdate(updateResponsePoll1);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/updatePollOptions", token, updateRequest);
        faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll2= faroResponse.getEntity();
        Assert.assertEquals(updateResponsePoll2.getPollOptions().size(), 3);
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(0).getOption(), updateResponsePoll1.getPollOptions().get(0).getOption());
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(1).getOption(), updateResponsePoll1.getPollOptions().get(1).getOption());
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(0).getId(), updateResponsePoll1.getPollOptions().get(0).getId());
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(1).getId(), updateResponsePoll1.getPollOptions().get(1).getId());
        
    }
    
    @Test
    public void castVote() throws Exception{
    	List<PollOption> pollOptions = new ArrayList<PollOption>();
    	pollOptions.add(new PollOption("Italian"));
    	pollOptions.add(new PollOption("Indian"));
    	pollOptions.add(new PollOption("Burmese"));
    	Poll p = new Poll(eventId, "Kaivan", pollOptions, "Kaivan", "Dinner cuisine");
    	
    	// Create sample poll
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/create", token, p);
        Poll pollResponse = response.getEntity(Poll.class);
        assertEntity(p, pollResponse);
        
        // Cast vote: only add
        List<String> add = new ArrayList<String>();
        add.add(pollResponse.getPollOptions().get(0).getId());
        add.add(pollResponse.getPollOptions().get(1).getId());
        
        UpdateCollectionRequest<Poll, String> updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeAdded(add);
        updateRequest.setUpdate(pollResponse);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/castVote", token, updateRequest);
        FaroResponse<Poll> faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll= faroResponse.getEntity();
        
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(0).getVoters().size(), 1);
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(1).getVoters().size(), 1);
        Assert.assertEquals(updateResponsePoll.getPollOptions().get(2).getVoters().size(), 0);
        Assert.assertTrue(updateResponsePoll.getPollOptions().get(0).getVoters().contains(callerEmail));
        Assert.assertTrue(updateResponsePoll.getPollOptions().get(1).getVoters().contains(callerEmail));
        Assert.assertFalse(updateResponsePoll.getPollOptions().get(2).getVoters().contains(callerEmail));
        
        // Cast Vote: only remove
        List<String> remove = new ArrayList<String>();
        remove.add(updateResponsePoll.getPollOptions().get(0).getId());
        updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeRemoved(remove);
        updateRequest.setUpdate(updateResponsePoll);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/castVote", token, updateRequest);
        faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll1= faroResponse.getEntity();
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(0).getVoters().size(), 0);
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(1).getVoters().size(), 1);
        Assert.assertEquals(updateResponsePoll1.getPollOptions().get(2).getVoters().size(), 0);
        Assert.assertFalse(updateResponsePoll1.getPollOptions().get(0).getVoters().contains(callerEmail));
        Assert.assertTrue(updateResponsePoll1.getPollOptions().get(1).getVoters().contains(callerEmail));
        Assert.assertFalse(updateResponsePoll1.getPollOptions().get(2).getVoters().contains(callerEmail));
        
        // Update: add and remove
        add.clear();remove.clear();
        add.add(updateResponsePoll1.getPollOptions().get(2).getId());
        remove.add(updateResponsePoll1.getPollOptions().get(1).getId());
        updateRequest = new UpdateCollectionRequest<>();
        updateRequest.setToBeAdded(add);
        updateRequest.setToBeRemoved(remove);
        updateRequest.setUpdate(updateResponsePoll1);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/poll/"
        		+pollResponse.getId()+"/castVote", token, updateRequest);
        faroResponse = response.getEntity(new GenericType<FaroResponse<Poll>>(){});
        Poll updateResponsePoll2= faroResponse.getEntity();
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(0).getVoters().size(), 0);
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(1).getVoters().size(), 0);
        Assert.assertEquals(updateResponsePoll2.getPollOptions().get(2).getVoters().size(), 1);
        Assert.assertFalse(updateResponsePoll2.getPollOptions().get(0).getVoters().contains(callerEmail));
        Assert.assertFalse(updateResponsePoll2.getPollOptions().get(1).getVoters().contains(callerEmail));
        Assert.assertTrue(updateResponsePoll2.getPollOptions().get(2).getVoters().contains(callerEmail));
        
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
    }
}
