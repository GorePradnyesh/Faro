package com.zik.faro.api.unit;


import java.util.GregorianCalendar;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.event.EventCreateHandler;
import com.zik.faro.api.event.EventHandler;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.applogic.EventManagement;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;

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

        EventCreateData eventCreateData = new EventCreateData(eventName, new GregorianCalendar(),
                new GregorianCalendar(), new Location("Random Location"), null);

        EventCreateHandler eventCreateHandler = new EventCreateHandler();
        Event event = eventCreateHandler.createEvent(signature, eventCreateData);

        String eventId = event.getEventId();

        EventHandler eventHandler = new EventHandler();
        Event retrievedEvent = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(eventId, retrievedEvent.getEventId());
        Assert.assertEquals(eventName, retrievedEvent.getEventName());
    }

    @Test
    public void testEventControlFlag(){
        String signature = "dummySignature";
        String eventName = UUID.randomUUID().toString();
        EventCreateHandler eventCreateHandler = new EventCreateHandler();
        EventHandler eventHandler = new EventHandler();

        //Store event
        EventCreateData eventCreateData = new EventCreateData(eventName, new GregorianCalendar(),
        		new GregorianCalendar(), new Location("Random Location"), null);
        Event minEvent = eventCreateHandler.createEvent(signature, eventCreateData);

        String eventId = minEvent.getEventId();
        //Retrieve event, verify control flag
        Event event = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(false, event.isControlFlag());

        // Disable control flag
        eventHandler.disableEventControl(signature, eventId);
        
        //Retrieve event again, verify control flag
        event = eventHandler.getEventDetails(signature, eventId);
        Assert.assertEquals(true, event.isControlFlag());
    }

    @Test
    public void testEventControlFlagBadEventId(){
        String signature = "dummySignature";
        String eventName = UUID.randomUUID().toString();

        EventHandler eventHandler = new EventHandler();
        boolean correctExceptionTriggered = false;
        // Disable control flag
        try {
            eventHandler.disableEventControl(signature, "BADEVENTID");
        }catch(WebApplicationException ex){
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
            correctExceptionTriggered = true;
        }
        Assert.assertTrue(correctExceptionTriggered);
    }
}
