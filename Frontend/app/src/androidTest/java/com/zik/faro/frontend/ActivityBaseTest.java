package com.zik.faro.frontend;

import android.app.Application;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.EventCreateData;
import com.zik.faro.data.Location;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class ActivityBaseTest extends ApiBaseTest {
    public ActivityBaseTest() {
        super(Application.class);
    }

    @LargeTest
    public void testCreateGetActivity() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString() + "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        boolean timeout;

        // Create Event
        TestEventCreateCallback createCallback = new TestEventCreateCallback(waitSem, 200, null);
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(), null, null, null);
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
        
        // Create Activity 
        String activityName = UUID.randomUUID().toString();
        Activity activity = new Activity(eventId, activityName, "randomDescription", new Location("Location1"), Calendar.getInstance());
        TestActivityCreateCallback createActivityCallback = new TestActivityCreateCallback(waitSem, 200);
        serviceHandler.getActivityHandler().createActivity(createActivityCallback, eventId, activity);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertNotNull(createActivityCallback.receivedActivity);
        Assert.assertFalse(createActivityCallback.failed);
        Assert.assertFalse(createActivityCallback.unexpectedResponseCode);
        
        // Get Activity. TODO: Add the remaining tests here
    }

    static class TestActivityCreateCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<Activity> {
        TestActivityCreateCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }
        Activity receivedActivity;
        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
            this.failed = true;
        }

        @Override
        public void onResponse(Activity activity, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            else
            {
                this.receivedActivity = activity;
            }
            waitSem.release();
        }
    }
}
