package com.zik.faro.persistence.datastore;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.Poll;
import com.zik.faro.data.expense.ExpenseGroup;

import org.junit.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class PollDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(Poll.class);
        ObjectifyService.register(Event.class);
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
    
    private Event createEvent(){
    	String eventNameSuffix = UUID.randomUUID().toString();
        Event testEvent = new Event("Lake Shasta "+ eventNameSuffix,
        		new GregorianCalendar(),
        		new GregorianCalendar(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));

        EventDatastoreImpl.storeEventOnly(testEvent);
        return testEvent;
    }

    private Poll createPollObjectForEventId(final String eventId){
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

    @Test
    public void testPollLoadStore() throws DataNotFoundException{
        Event event = createEvent();
        Poll dummyPoll = createPollObjectForEventId(event.getEventId());
        PollDatastoreImpl.storePoll(dummyPoll);
        Poll retPoll = PollDatastoreImpl.loadPollById(dummyPoll.getId(), event.getEventId());
        Assert.assertNotNull(retPoll);
        Assert.assertEquals(retPoll.getEventId(), event.getEventId());
    }

    @Test
    public void testLoadPollsForEvent() throws DataNotFoundException{
    	Event event = createEvent();
    	String eventId = event.getEventId();
        Poll poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        Poll poll2 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll2);

        List<Poll> polls = PollDatastoreImpl.loadPollsByEventId(eventId);
        Assert.assertEquals(2, polls.size());
    }

}

