package com.zik.faro.frontend;


import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.frontend.data.DateOffset;
import com.zik.faro.frontend.data.Event;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class PollApiTest extends ApiBaseTest{
    public PollApiTest() {
        super(Application.class);
    }

    @LargeTest
    public void testCreateGetPoll() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString() + "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        boolean timeout;

        // Create Event
        TestEventCreateCallback createCallback = new TestEventCreateCallback(waitSem, 200, null);
        EventCreateData eventCreateData= new EventCreateData("MySampleEvent", new DateOffset(new Date(), 1000), null, null, null);
        serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        timeout = false;
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(createCallback.failed);
        Assert.assertFalse(createCallback.unexpectedResponseCode);
        String eventId = createCallback.receivedEvent.getEventId();
        
        // Get Event;
        TestGetEventCallbackHandler callback = new TestGetEventCallbackHandler(waitSem, 200);
        serviceHandler.getEventHandler().getEvent(callback, eventId);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        
        Assert.assertNotNull(callback.receivedEvent);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
        
        // Create poll
        String optionValue = UUID.randomUUID().toString();
        Poll.PollOption option1 = new Poll.PollOption(optionValue);
        List<Poll.PollOption> pollOptions = new ArrayList<>();
        pollOptions.add(option1);
        Poll poll1 = new Poll(eventId, uuidEmail, pollOptions, uuidEmail, "Desc1");
        TestPollCreateCallback pollCreateCallback = new TestPollCreateCallback(waitSem, 200);
        serviceHandler.getPollHandler().createPoll(pollCreateCallback, eventId, poll1);
        
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(pollCreateCallback.failed);
        Assert.assertFalse(pollCreateCallback.unexpectedResponseCode);
    }


    final static class TestPollCreateCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<String> {
        TestPollCreateCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
        }

        @Override
        public void onResponse(String s, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            waitSem.release();    
        }
    }
}
