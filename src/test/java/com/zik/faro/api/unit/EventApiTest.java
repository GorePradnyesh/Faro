package com.zik.faro.api.unit;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.event.EventCreateHandler;
import com.zik.faro.api.event.EventHandler;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import org.junit.*;

import java.util.Date;
import java.util.UUID;

public class EventApiTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(50));

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

    @After
    public void tearDown() {
        helper.tearDown();
    }


    @Test
    public void testEventCreation(){
        String signature = "dummySignature";
        String eventName = UUID.randomUUID().toString();

        EventCreateData eventCreateData = new EventCreateData(eventName, new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"), null);

        EventCreateHandler eventCreateHandler = new EventCreateHandler();
        EventManagement.MinEvent minEvent = eventCreateHandler.createEvent(signature, eventCreateData);

        String eventId = minEvent.id;

        EventHandler eventHandler = new EventHandler();
        Event event = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(eventId, event.getEventId());
        Assert.assertEquals(eventName, event.getEventName());
    }

    @Test
    public void testEventControlFlag(){
        String signature = "dummySignature";
        String eventName = UUID.randomUUID().toString();
        EventCreateHandler eventCreateHandler = new EventCreateHandler();
        EventHandler eventHandler = new EventHandler();

        //Store event
        EventCreateData eventCreateData = new EventCreateData(eventName, new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"), null);
        EventManagement.MinEvent minEvent = eventCreateHandler.createEvent(signature, eventCreateData);

        String eventId = minEvent.id;
        //Retrieve event, verify control flag
        Event event = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(false, event.isControlFlag());

        // Disable control flag
        String response = eventHandler.disableEventControl(signature, eventId);
        Assert.assertEquals(Constants.HTTP_OK, response);

        //Retrieve event again, verify control flag
        event = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(true, event.isControlFlag());
    }
}
