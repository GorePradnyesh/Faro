package com.zik.faro.frontend;


import android.app.Application;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.data.PollOption;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
        String uuidEmail = UUID.randomUUID().toString() + "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        boolean timeout;

        // Create Event
        TestEventCreateCallback createCallback = new TestEventCreateCallback(waitSem, 200, null);
        Event eventCreateData= new Event("MySampleEvent", Calendar.getInstance(), null, null, false,
                null, null, null, null, null);
        serviceHandler.getEventHandler().createEvent(createCallback, eventCreateData);
        timeout = false;
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(createCallback.failed);
        Assert.assertFalse(createCallback.unexpectedResponseCode);
        String eventId = createCallback.receivedEvent.getId();
        
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
        String optionValue2 = UUID.randomUUID().toString();
        PollOption option1 = new PollOption(optionValue);
        PollOption option2 = new PollOption(optionValue2);        
        List<PollOption> pollOptions = new ArrayList<>();
        pollOptions.add(option1);
        pollOptions.add(option2);
        Poll poll1 = new Poll(eventId, uuidEmail, pollOptions, uuidEmail, "Desc1");
        TestPollCreateCallback pollCreateCallback = new TestPollCreateCallback(waitSem, 200);
        serviceHandler.getPollHandler().createPoll(pollCreateCallback, eventId, poll1);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertNotNull(pollCreateCallback.receivedPoll);
        Assert.assertFalse(pollCreateCallback.failed);
        Assert.assertFalse(pollCreateCallback.unexpectedResponseCode);
        Poll createdPoll = pollCreateCallback.receivedPoll;
        
        // Get Poll
        TestPollGetCallback getPollCallback = new TestPollGetCallback(waitSem, 200);
        serviceHandler.getPollHandler().getPoll(getPollCallback, eventId, createdPoll.getId());
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getPollCallback.failed);
        Assert.assertFalse(getPollCallback.unexpectedResponseCode);
        Poll receivedPoll = getPollCallback.receivedPoll;
        Assert.assertNotNull(receivedPoll);
        Assert.assertTrue(poll1.getCreatorId().equals(receivedPoll.getCreatorId()));
        Assert.assertTrue(receivedPoll.getStatus() == ObjectStatus.OPEN);
        
        
        // Cast Vote
        //TODO: finish implementation at the backend
        Set<String> options = new HashSet<>();
        options.add(optionValue);
        TestOKActionCallbackHandler castVoteCallback = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getPollHandler().castVote(castVoteCallback, eventId, receivedPoll.getId(), options);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(castVoteCallback.failed);
        Assert.assertFalse(castVoteCallback.unexpectedResponseCode);
        
        
        /*        
        // Close Poll
        //TODO: finish implementation at the backend 
        TestPollCloseCallback pollCloseCallback = new TestPollCloseCallback(waitSem, 200);
        serviceHandler.getPollHandler().closePoll(pollCloseCallback, eventId, poll1.getId());
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(pollCloseCallback.failed);
        Assert.assertFalse(pollCloseCallback.unexpectedResponseCode);
        */

        // Get unvoted count
        //TODO: finish implementation at the backend
        TestPollUnvotedCountCallback unvotedCountCallback = new TestPollUnvotedCountCallback(waitSem, 200);
        serviceHandler.getPollHandler().getUnvotedCount(unvotedCountCallback, eventId);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(unvotedCountCallback.failed);
        Assert.assertFalse(unvotedCountCallback.unexpectedResponseCode);
        int i = unvotedCountCallback.unvotedCount;
        /**/
        
        // Delete Poll
        TestOKActionCallbackHandler deletePollCallBack = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getPollHandler().deletePoll(deletePollCallBack, eventId, receivedPoll.getId());
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(deletePollCallBack.failed);
        Assert.assertFalse(deletePollCallBack.unexpectedResponseCode);
        
        // Verify that poll has been deleted
        getPollCallback = new TestPollGetCallback(waitSem, 404);
        serviceHandler.getPollHandler().getPoll(getPollCallback, eventId, receivedPoll.getId());
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertTrue(getPollCallback.failed);
        Assert.assertFalse(getPollCallback.unexpectedResponseCode);
        receivedPoll = getPollCallback.receivedPoll;
        Assert.assertNull(receivedPoll);

    }


    static class TestPollGetCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<Poll> {
        public Poll receivedPoll;
        TestPollGetCallback(Semaphore semaphore, int expectedCode) {
            super(semaphore, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            waitSem.release();
            this.failed = true;
        }

        @Override
        public void onResponse(Poll poll, HttpError error) {
            if(error != null){
                this.failed = true;
                if(error.code != expectedCode)
                {
                    this.unexpectedResponseCode = true;
                }
            }
            this.receivedPoll = poll;
            waitSem.release();
        }

    }

    static class TestPollUnvotedCountCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<Integer> {
        public int unvotedCount;
        TestPollUnvotedCountCallback(Semaphore semaphore, int expectedCode) {
            super(semaphore, expectedCode);
            this.unvotedCount = 0;
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            this.failed = true;
            waitSem.release();
        }

        @Override
        public void onResponse(Integer integer, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            else {
                this.unvotedCount = integer.intValue();
            }
            waitSem.release();
        }
    }
    
    
    static class TestPollCreateCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<Poll> {
        Poll receivedPoll;
        TestPollCreateCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }
    
        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
            this.failed = true;
        }

        @Override
        public void onResponse(Poll poll, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            else{
                this.receivedPoll = poll;
            }
            waitSem.release();    
        }
    }
    

    static class TestPollCloseCallback extends TestPollCreateCallback{
        TestPollCloseCallback(Semaphore semaphore, int expectedCode) {
            super(semaphore, expectedCode);
        }
    }
}
