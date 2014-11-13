package com.zik.faro.persistence.datastore;

import java.util.Date;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.expense.ExpenseGroup;

public class EventDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
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

    @AfterClass
    public static void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testSimpleStoreLoad(){
        String eventId = UUID.randomUUID().toString();
        Event testEvent = new Event("Lake Shasta "+ eventId,
                new DateOffset(new Date(), 60 * 1000),
                new DateOffset(new Date(), 2 * 60* 1000),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));

        Key<Event> key = EventDatastoreImpl.storeEvent(testEvent);
        Event loadedEvent = EventDatastoreImpl.loadEvent(key);
        Assert.assertNotNull(loadedEvent);
        Assert.assertEquals(loadedEvent.getEventName(), testEvent.getEventName());
    }
}
