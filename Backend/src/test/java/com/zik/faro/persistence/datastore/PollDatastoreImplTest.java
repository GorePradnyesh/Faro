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
import com.zik.faro.data.PollOption;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.EventDo;
import com.zik.faro.data.Location;
import com.zik.faro.data.PollDo;
import com.zik.faro.data.expense.ExpenseGroup;

public class PollDatastoreImplTest {

	private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(50));

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
        options.add(new PollOption("Shasta"));
        PollOption vegasOption = new PollOption("Las Vegas");
        vegasOption.addVoters("user1");
        vegasOption.addVoters("user2");
        options.add(vegasOption);
        PollDo dummyPoll = new PollDo(eventId, "dummyCreator", options, "dummyOwner", "sample skeleton poll");
        dummyPoll.setWinnerId(vegasOption.getId());
        return dummyPoll;
    }

    @Test
    public void testPollLoadStore() throws DataNotFoundException, JsonGenerationException, JsonMappingException, IOException{
        EventDo event = createEvent();
        PollDo dummyPoll = createPollObjectForEventId(event.getEventId());
        PollDatastoreImpl.storePoll(dummyPoll);
        PollDo retPoll = PollDatastoreImpl.loadPollById(dummyPoll.getId(), event.getEventId());
        Assert.assertNotNull(retPoll);
        Assert.assertEquals(retPoll.getEventId(), event.getEventId());
        
    }

    @Test
    public void testLoadPollsForEvent() throws DataNotFoundException{
    	EventDo event = createEvent();
    	String eventId = event.getEventId();
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
    	String eventId = event.getEventId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        PollDo poll2 = createPollObjectForEventId(eventId);
        poll2.getPollOptions().get(0).addVoters("user4");
        poll2.getPollOptions().get(0).addVoters("user5");
        poll2.getPollOptions().get(1).addVoters("user5");
        PollDatastoreImpl.storePoll(poll2);
        // User1 has voted in both polls
        int count = PollDatastoreImpl.getCountofUnvotedPolls(event.getEventId(), "user1");
        Assert.assertEquals(0, count);
        // User3 has not voted in any of the polls
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getEventId(), "user3");
        Assert.assertEquals(2, count);
        // User4 has voted for one poll
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getEventId(), "user4");
        Assert.assertEquals(1, count);
        // User5 has voted for both options of a single poll.
        // This should also just be counted as 1 voted poll since we dont care how many options he votes for in a single poll
        count = PollDatastoreImpl.getCountofUnvotedPolls(event.getEventId(), "user5");
        Assert.assertEquals(1, count);
    }
    
    @Test
    public void testCastVote() throws DataNotFoundException, DatastoreException{
    	EventDo event = createEvent();
    	String eventId = event.getEventId();
        PollDo poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        Set<String> optionIds = new HashSet<String>();
        optionIds.add(poll1.getPollOptions().get(0).getId());
        optionIds.add(poll1.getPollOptions().get(1).getId());
        // Cast user3's vote to both options of the poll(Shasta and vegas)
        PollDatastoreImpl.castVote(event.getEventId(), poll1.getId(), optionIds, "user3");
        
        // Verify. If vote went through, then count of unvoted polls will be zero
        Assert.assertEquals(0, PollDatastoreImpl.getCountofUnvotedPolls(eventId, "user3"));
    }
    
    @Test
    public void testDeletePoll() throws DataNotFoundException{
    	EventDo event = createEvent();
    	String eventId = event.getEventId();
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

