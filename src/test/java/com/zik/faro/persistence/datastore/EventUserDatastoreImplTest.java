package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.Location;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import org.junit.*;

import java.util.Date;
import java.util.UUID;

public class EventUserDatastoreImplTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(EventUser.class);
        ObjectifyService.register(Event.class);
        ObjectifyService.register(FaroUser.class);
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
    }

    @Before
    public void setUp() {
        helper.setUp();
    }

    @AfterClass
    public static void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testEventUserLoadStore(){
        final String eventName = UUID.randomUUID().toString();
        Event testEvent = new Event(eventName,
                new DateOffset(new Date(), 60 * 1000),
                new DateOffset(new Date(), 2 * 60* 1000),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));
        DatastoreObjectifyDAL.storeObject(testEvent);


        FaroUser faroUser = new FaroUser("user@gmail.com", "SampleUser", null, "Gilmour", "dg@splitwise.com",
                "2323", new Address(123, "Palm Avenue", "Stanford", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser);

        EventUserDatastoreImpl.storeEventUser(testEvent.getEventId(), faroUser.getEmail());
        EventUser eventUser = EventUserDatastoreImpl.loadEventUser(testEvent.getEventId(), faroUser.getEmail());
        Assert.assertNotNull(eventUser);

        Event retEvent = eventUser.getEvent();
        Assert.assertEquals(testEvent.getEventName(), retEvent.getEventName());

        FaroUser retFaroUser = eventUser.getFaroUser();
        Assert.assertEquals(faroUser.getFirstName(), retFaroUser.getFirstName());

    }
}
