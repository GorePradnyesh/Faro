package com.zik.faro.frontend;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class ApiBaseTest extends ApplicationTestCase<Application> {
    protected static final int testTimeout = 30000;
    
    public ApiBaseTest(Class<Application> applicationClass) {
        super(applicationClass);
    }

    protected String getTokenForNewUser(final String username, final String password) throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        Utils.TestSignupCallback callback = new Utils.TestSignupCallback(waitSem, 200);
        serviceHandler.getSignupHandler().signup(callback, new FaroUser(username, null, null, null, null, null, null), password);
        boolean timeout;
        timeout = !waitSem.tryAcquire(30000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        return callback.token;
    }

    protected void signUpUser(final String username, final String password) throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
        Utils.TestSignupCallback callback = new Utils.TestSignupCallback(waitSem, 200);
        serviceHandler.getSignupHandler().signup(callback, new FaroUser(username, null, null, null, null, null, null), password, false);
        boolean timeout;
        timeout = !waitSem.tryAcquire(30000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
    }
    
    // ------------------------ Helpers 


    final static class TestGetEventsCallback
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<List<EventInviteStatusWrapper>> {
        List<Event> eventList;
        TestGetEventsCallback(Semaphore semaphore){
            super(semaphore, 0);
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            waitSem.release();
        }

        @Override
        public void onResponse(List<EventInviteStatusWrapper> eventInviteStatusWrappers, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            this.eventList = eventList;
            waitSem.release();
        }
    }

    final static class TestEventCreateCallback
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<Event>{
        Event expectedEvent;
        Event receivedEvent;
        TestEventCreateCallback(Semaphore semaphore, int expectedCode, Event expectedEvent){
            super(semaphore, expectedCode);
            this.expectedEvent = expectedEvent;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
        }

        @Override
        public void onResponse(Event event, HttpError error){
            if(error != null){
                this.failed = true;
            }
            // Assert that event == min event
            this.receivedEvent = event;
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


    final static class TestOKActionCallbackHandler<String>
            extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<String>{
        TestOKActionCallbackHandler(final Semaphore sem, int expectedCode){
            super(sem, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.v("Get Event", "Failed");
            failed = true;
            waitSem.release();
        }

        @Override
        public void onResponse(String s, HttpError error){
            if(error != null){
                this.failed = true;
            }
            else
            {
                this.failed = false;
            }
            Log.v("Get Event", "Success");
            waitSem.release();
        }
    }
}
