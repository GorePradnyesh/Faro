package com.zik.faro.frontend;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.zik.faro.frontend.data.DateOffset;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.MinEvent;
import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperEvent;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class EventApiTest extends ApplicationTestCase<Application> {
    public EventApiTest() {
        super(Application.class);
    }
    private static final String baseUrl = "http://10.0.2.2:8080/v1/";

    private String getTokenForNewUser(final String username, final String password) throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));

        Utils.TestSignupCallback callback = new Utils.TestSignupCallback(waitSem, 200);
        serviceHandler.getSignupHandler().signup(callback, new FaroUser(username), password);
        boolean timeout;
        timeout = !waitSem.tryAcquire(30000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        return callback.token;
    }
    
    @LargeTest
    public void testGetEvent() throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));


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
        EventCreateData eventCreateData= new EventCreateData("MySampleEvent", new DateOffset(new Date(), 1000), null, null, null);
        serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        timeout = false;
        timeout = !waitSem.tryAcquire(10000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(createCallback.failed);
        Assert.assertFalse(createCallback.unexpectedResponseCode);

        // Get Event
        String eventId = createCallback.receivedMinEvent.id;
        TestGetEventCallbackHandler callback = new TestGetEventCallbackHandler(waitSem, 200);
        serviceHandler.getEventHandler().getEvent(callback, eventId);
        timeout = !waitSem.tryAcquire(3000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        // TODO: check why start date is null for received event
        Assert.assertNotNull(callback.receivedEvent);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
    }

    @LargeTest
    public void testCreateEvent() throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        OKHttpWrapperEvent reqEvent = new OKHttpWrapperEvent(new URL(baseUrl));
        TestEventCreateCallback callback = new TestEventCreateCallback(waitSem, 200, null);

        EventCreateData eventCreateData= new EventCreateData("MySampleEvent", new DateOffset(new Date(), 1000), null, null, null);

        reqEvent.createEvent(callback, eventCreateData);
        boolean timeout = false;
        timeout = !waitSem.tryAcquire(3000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
    }

    @LargeTest
    public void testGetEvents() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        boolean timeout = false;

        // Get event list
        TestGetEventsCallback getEventsCallback 
                = new TestGetEventsCallback(waitSem);
        serviceHandler.getEventHandler().getEvents(getEventsCallback);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getEventsCallback.failed);
        Assert.assertTrue(getEventsCallback.eventList.size() > 0);
        System.out.println("Got " + getEventsCallback.eventList.size() + "Events");
    }
    
    // ------------------------ Helpers 

    
    final static class TestGetEventsCallback 
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<List<Event>>{
        List<Event> eventList;
        TestGetEventsCallback(Semaphore semaphore){
            super(semaphore, 0);
        }
        
        @Override
        public void onFailure(Request request, IOException ex) {
            waitSem.release();    
        }

        @Override
        public void onResponse(List<Event> eventList, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            this.eventList = eventList;
            waitSem.release();
        }
    }
    
    final static class TestEventCreateCallback
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<MinEvent>{
        MinEvent expectedMinEvent;
        MinEvent receivedMinEvent;
        TestEventCreateCallback(Semaphore semaphore, int expectedCode, MinEvent expectedEvent){
            super(semaphore, expectedCode);
            this.expectedMinEvent = expectedEvent;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
        }

        @Override
        public void onResponse(MinEvent event, HttpError error){
            if(error != null){
                this.failed = true;               
            }
            // Assert that event == min event
            this.receivedMinEvent = event;
            waitSem.release();
        }
    }

    final static class TestGetEventCallbackHandler<Event>
            extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<Event>{
        public Event receivedEvent;
        TestGetEventCallbackHandler(final Semaphore sem, int expectedCode){
            super(sem, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.v("Get Event", "Failed");
            failed = true;
            waitSem.release();
        }

        @Override
        public void onResponse(Event event, HttpError error){
            if(event != null){
                this.receivedEvent = event;
            }
            Log.v("Get Event", "Success");
            waitSem.release();
        }
    }
}