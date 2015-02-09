package com.zik.faro.persistence.datastore;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.Poll;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

public class PollDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(Poll.class);
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
    public void testPollLoadStore(){
        String eventId = "sampleEventId";
        Poll dummyPoll = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(dummyPoll);
        Poll retPoll = PollDatastoreImpl.loadPollById(dummyPoll.getId(), eventId);
        Assert.assertNotNull(retPoll);
        Assert.assertEquals(retPoll.getEventId(), eventId);
    }

    @Test
    public void testLoadPollsForEvent(){
        String eventId = "testEventId";
        Poll poll1 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll1);
        Poll poll2 = createPollObjectForEventId(eventId);
        PollDatastoreImpl.storePoll(poll2);

        List<Poll> polls = PollDatastoreImpl.loadPollsByEventId(eventId);
        Assert.assertEquals(2, polls.size());
    }

}

