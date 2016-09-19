package com.zik.faro.persistence.datastore;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;

public class PollDatastoreImplTest {

	private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    static{
        ObjectifyService.register(PollDo.class);
        ObjectifyService.register(EventDo.class);
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
    }

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
    
    private EventDo createEvent(){
    	String eventNameSuffix = UUID.randomUUID().toString();
        EventDo testEvent = new EventDo("Lake Shasta "+ eventNameSuffix,
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));

        EventDatastoreImpl.storeEventOnly(testEvent);
        return testEvent;
    }

    private PollDo createPollObjectForEventId(final String eventId){
        List<PollOption> options = new ArrayList<>();
        options.add(new PollOption(UUID.randomUUID().toString(),"Shasta"));
        PollOption vegasOption = new PollOption(UUID.randomUUID().toString(),"Las Vegas");
        vegasOption.addVoters("user1");
        vegasOption.addVoters("user2");
        options.add(vegasOption);
        PollOption sfOption = new PollOption(UUID.randomUUID().toString(),"SF");
        sfOption.addVoters("user1");
        sfOption.addVoters("user2");
        options.add(sfOption);
        PollDo dummyPoll = new PollDo(eventId, "dummyCreator", options, vegasOption.getId(), "dummyOwner", "sample skeleton poll", ObjectStatus.OPEN,
        		Calendar.getInstance(), false);
        return dummyPoll;
    }

    @Test
    public void testPollLoadStore() throws DataNotFoundException, JsonGenerationException, JsonMappingException, IOException{
        EventDo event = createEvent();
        PollDo dummyPoll = createPollObjectForEventId(event.getId());
        PollDatastoreImpl.storePoll(dummyPoll);
        PollDo retPoll = PollDatastoreImpl.loadPollById(dummyPoll.getId(), event.getId());
        Assert.assertNotNull(retPoll);
    }

    @Test
    public void testLoadPollsForEvent() throws DataNotFoundException{
    	EventDo event = createEvent();
    	String eventId = event.getId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        PollDo poll2 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll2);

        List<PollDo> polls = PollDatastoreImpl.loadPollsByEventId(eventId);
        Assert.assertEquals(2, polls.size());
    }
    
    @Test
    public void testCountOfUnvotedPolls() throws DataNotFoundException{
    	EventDo event = createEvent();
    	String eventId = event.getId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        PollDo poll2 = createPollObjectForEventId(eventId);
        poll2.getPollOptions().get(0).addVoters("user4");
        poll2.getPollOptions().get(0).addVoters("user5");
        poll2.getPollOptions().get(1).addVoters("user5");
        PollDatastoreImpl.storePoll(poll2);
        // User1 has voted in both polls
        int count = PollDatastoreImpl.getCountofUnvotedPolls(event.getId(), "user1");
        Assert.assertEquals(0, count);
        // User3 has not voted in any of the polls
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getId(), "user3");
        Assert.assertEquals(2, count);
        // User4 has voted for one poll
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getId(), "user4");
        Assert.assertEquals(1, count);
        // User5 has voted for both options of a single poll.
        // This should also just be counted as 1 voted poll since we dont care how many options he votes for in a single poll
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getId(), "user5");
        Assert.assertEquals(1, count);
    }
    
    @Test
    public void testCastVote() throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	EventDo event = createEvent();
    	String eventId = event.getId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        Set<String> optionIds = new HashSet<String>();
        optionIds.add(poll1.getPollOptions().get(0).getId());
        optionIds.add(poll1.getPollOptions().get(1).getId());
        System.out.println(poll1.getPollOptions());
        PollDo updateObj = new PollDo();
        updateObj.setId(poll1.getId());updateObj.setEventId(poll1.getEventId());updateObj.setVersion(poll1.getVersion());
        
        // Cast user3's vote to both options of the poll(Shasta and vegas)
        PollDatastoreImpl.updatePoll(event.getId(), poll1.getId(), updateObj, "user3", optionIds);
        
        // Verify. If vote went through. User3 should be present in both shasta and vegas
        PollDo returnedPollDo = PollDatastoreImpl.loadPollById(poll1.getId(), eventId);
        Assert.assertTrue(returnedPollDo.getPollOptions().get(0).getVoters().contains("user3"));
        Assert.assertTrue(returnedPollDo.getPollOptions().get(1).getVoters().contains("user3"));
        Assert.assertFalse(returnedPollDo.getPollOptions().get(2).getVoters().contains("user3"));
        
        // CHange user3s vote and verify
        optionIds = new HashSet<String>();
        optionIds.add(poll1.getPollOptions().get(2).getId());
        updateObj.setVersion(returnedPollDo.getVersion());
        PollDatastoreImpl.updatePoll(event.getId(), poll1.getId(), updateObj, "user3", optionIds);
        returnedPollDo = PollDatastoreImpl.loadPollById(poll1.getId(), eventId);
        Assert.assertFalse(returnedPollDo.getPollOptions().get(0).getVoters().contains("user3"));
        Assert.assertFalse(returnedPollDo.getPollOptions().get(1).getVoters().contains("user3"));
        Assert.assertTrue(returnedPollDo.getPollOptions().get(2).getVoters().contains("user3"));
        try{
        	PollDatastoreImpl.updatePoll(event.getId(), poll1.getId(), updateObj, "user3", optionIds);
        }catch(UpdateVersionException e){
        	Assert.assertNotNull(e);
        	Assert.assertEquals("Incorrect entity version. Current version:3", e.getMessage());
        }
    }
    
    @Test
    public void testUpdatePoll() throws DataNotFoundException, DatastoreException, UpdateVersionException{
    	EventDo event = createEvent();
    	String eventId = event.getId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        
        // Update poll 
        PollDo updateObj = new PollDo();
        updateObj.setId(poll1.getId()); updateObj.setEventId(poll1.getEventId());
        updateObj.setCreatorId("Updated creator");
        updateObj.setDeadline(Calendar.getInstance());
        updateObj.setDescription("Updated description");
        updateObj.setStatus(ObjectStatus.CLOSED);
        updateObj.setWinnerId("Updated poll winner");
        //updateObj.setMultiChoice(true);
        updateObj.addPollOptions(new PollOption(UUID.randomUUID().toString(), "Bombay"));
        updateObj.setVersion(1L);
        
        // Call update API
        PollDatastoreImpl.updatePoll(updateObj.getEventId(), updateObj.getId(), updateObj, null, null);
        
        // Verify. 
        PollDo returnedPollDo = PollDatastoreImpl.loadPollById(poll1.getId(), eventId);
        Assert.assertEquals(returnedPollDo.getDescription(), updateObj.getDescription());
        Assert.assertEquals(returnedPollDo.getCreatorId(), updateObj.getCreatorId());
        Assert.assertEquals(returnedPollDo.getDeadline(), updateObj.getDeadline());
        Assert.assertEquals(returnedPollDo.getStatus(), updateObj.getStatus());
        Assert.assertEquals(returnedPollDo.getWinnerId(), updateObj.getWinnerId());
        //Assert.assertTrue(returnedPollDo.getMultiChoice());
        Assert.assertEquals(4, returnedPollDo.getPollOptions().size());
        Long newVersion = updateObj.getVersion();
        Assert.assertEquals(++newVersion, returnedPollDo.getVersion());
        
        // Another update with correct version
        updateObj = new PollDo();
        updateObj.setId(poll1.getId()); updateObj.setEventId(poll1.getEventId());
        updateObj.setCreatorId("Updated creator2");
        updateObj.setVersion(returnedPollDo.getVersion());
        
        returnedPollDo = PollDatastoreImpl.updatePoll(updateObj.getEventId(), updateObj.getId(), updateObj, null, null);
        Assert.assertEquals(returnedPollDo.getCreatorId(), updateObj.getCreatorId());
        newVersion = updateObj.getVersion();
        Assert.assertEquals(++newVersion, returnedPollDo.getVersion());
        
        //Another update with wrong version
        updateObj.setCreatorId("Updated creator3");
        try{
        	returnedPollDo = PollDatastoreImpl.updatePoll(updateObj.getEventId(), updateObj.getId(), updateObj, null, null);
        }catch(UpdateVersionException e){
        	Assert.assertNotNull(e);
        	Assert.assertEquals("Incorrect entity version. Current version:3", e.getMessage());
        	return;
        }
        Assert.assertNull("Problem with Poll updates");
    }
    
    @Test
    public void testDeletePoll() throws DataNotFoundException{
    	EventDo event = createEvent();
    	String eventId = event.getId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        
        // Verify indeed created
        PollDo p = PollDatastoreImpl.loadPollById(poll1.getId(), poll1.getEventId());
        Assert.assertNotNull(p);
        
        // Delete
        PollDatastoreImpl.deletePoll(poll1.getEventId(), poll1.getId());
        
        // Verify indeed deleted
        try{
        	p = PollDatastoreImpl.loadPollById(poll1.getId(), poll1.getEventId());
        }catch(DataNotFoundException e){
        	p = null;
        }
        Assert.assertNull(p);
    }
}

