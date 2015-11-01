package com.zik.faro.frontend;

import android.app.Application;
import android.test.suitebuilder.annotation.LargeTest;

import com.zik.faro.data.EventCreateData;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;

import junit.framework.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class EventApiTest extends ApiBaseTest {
    public EventApiTest() {
        super(Application.class);
    }
        
    @LargeTest
    public void testGetEvent() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        TestGetEventCallbackHandler callback = new TestGetEventCallbackHandler(waitSem, 404);
        serviceHandler.getEventHandler().getEvent(callback, "randomEventId");
        boolean timeout = false;
        timeout = !waitSem.tryAcquire(3000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
    }

    @LargeTest
    public void testCreateGetEvent() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();
        
        getTokenForNewUser(uuidEmail, password);
        boolean timeout;
        
        // Create Event
        TestEventCreateCallback createCallback = new TestEventCreateCallback(waitSem, 200, null);
        EventCreateData eventCreateData= new EventCreateData("MySampleEvent", Calendar.getInstance(), Calendar.getInstance(), null, null);
        serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        timeout = false;
        timeout = !waitSem.tryAcquire(3000, TimeUnit.SECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(createCallback.failed);
        Assert.assertFalse(createCallback.unexpectedResponseCode);

        // Get Event
        String eventId = createCallback.receivedEvent.getEventId();
        TestGetEventCallbackHandler callback = new TestGetEventCallbackHandler(waitSem, 200);
        serviceHandler.getEventHandler().getEvent(callback, eventId);
        timeout = !waitSem.tryAcquire(3000, TimeUnit.SECONDS);

        Assert.assertFalse(timeout);
        // TODO: check why start date is null for received event
        Assert.assertNotNull(callback.receivedEvent);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
    }
    
    @LargeTest
    public void testCreateGetEvents() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();
        
        getTokenForNewUser(uuidEmail, password);
        
        TestEventCreateCallback callback = new TestEventCreateCallback(waitSem, 200, null);
        EventCreateData eventCreateData= new EventCreateData("MySampleEvent", Calendar.getInstance(), null, null, null);
        
        // Create Event
        serviceHandler.getEventHandler().createEvent(callback, eventCreateData);
        boolean timeout = false;
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
        Assert.assertNotNull(callback.receivedEvent.getEventId());

        // Get Event
        String eventId = callback.receivedEvent.getEventId();
        TestGetEventCallbackHandler getEventCallback = new TestGetEventCallbackHandler(waitSem, 200);
        serviceHandler.getEventHandler().getEvent(getEventCallback, eventId);
        timeout = !waitSem.tryAcquire(3000, TimeUnit.SECONDS);
        Assert.assertNotNull(getEventCallback.receivedEvent);
        Assert.assertFalse(getEventCallback.failed);
        Assert.assertFalse(getEventCallback.unexpectedResponseCode);
        
        timeout = false;
        
        // Get event list
        TestGetEventsCallback getEventsCallback 
                = new TestGetEventsCallback(waitSem);
        serviceHandler.getEventHandler().getEvents(getEventsCallback);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        
        Assert.assertFalse(timeout);
        Assert.assertFalse(getEventsCallback.failed);
        Assert.assertTrue(getEventsCallback.eventList.size() > 0); 
        System.out.println("Got " + getEventsCallback.eventList.size() + "Events");
    }
    
}