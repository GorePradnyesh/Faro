package com.zik.faro.frontend;

import android.app.Application;
import android.test.ActivityTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(), null, null, false, null, null, null, null, null);
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
        
        // Create Activity 
        String activityName = UUID.randomUUID().toString();
        Activity activity = new Activity(eventId, activityName,
		GeoPosition geoPosition = new GeoPosition(0,0);
		"randomDescription", new Location("Location1", "Address1", geoPosition), Calendar.getInstance(), Calendar.getInstance(), null);
        TestActivityCreateCallback createActivityCallback = new TestActivityCreateCallback(waitSem, 200);
        serviceHandler.getActivityHandler().createActivity(createActivityCallback, eventId, activity);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertNotNull(createActivityCallback.receivedActivity);
        Assert.assertFalse(createActivityCallback.failed);
        Assert.assertFalse(createActivityCallback.unexpectedResponseCode);
        
        // Get Activity. 
        String activityId = createActivityCallback.receivedActivity.getId();
        createActivityCallback = new TestActivityCreateCallback(waitSem, 200);
        serviceHandler.getActivityHandler().getActivity(createActivityCallback, eventId, activityId);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertEquals(activityName, createActivityCallback.receivedActivity.getName());
        Assert.assertNotNull(createActivityCallback.receivedActivity);
        Assert.assertFalse(createActivityCallback.failed);
        Assert.assertFalse(createActivityCallback.unexpectedResponseCode);
        
        // Create Activity 
        String activityName2 = UUID.randomUUID().toString();
        Activity activity2 = new Activity(eventId, activityName,
		"randomDescription", new Location("Location1", "Address1", geoPosition), Calendar.getInstance(), Calendar.getInstance(), null);
        TestActivityCreateCallback createActivityCallback2 = new TestActivityCreateCallback(waitSem, 200);
        serviceHandler.getActivityHandler().createActivity(createActivityCallback2, eventId, activity);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertNotNull(createActivityCallback2.receivedActivity);
        Assert.assertFalse(createActivityCallback2.failed);
        Assert.assertFalse(createActivityCallback2.unexpectedResponseCode);
        
        // Get Activities
        TestGetActivitiesCallback getActivitiesCallback = new TestGetActivitiesCallback(waitSem, 200);
        serviceHandler.getActivityHandler().getActivities(getActivitiesCallback, eventId);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertNotNull(getActivitiesCallback.receivedActivities);
        Assert.assertTrue(getActivitiesCallback.receivedActivities.size() > 1);
        Assert.assertFalse(getActivitiesCallback.failed);
        Assert.assertFalse(getActivitiesCallback.unexpectedResponseCode);
        
        // Delete
        TestOKActionCallbackHandler deleteCallback = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getActivityHandler().deleteActivity(deleteCallback, eventId, activity.getId());
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(deleteCallback.failed);
        Assert.assertFalse(deleteCallback.unexpectedResponseCode);
        
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

    static class TestGetActivitiesCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<List<Activity>> {
        TestGetActivitiesCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }
        List<Activity> receivedActivities;
        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
            this.failed = true;
        }

        @Override
        public void onResponse(List<Activity> activities, HttpError error) {
            if(error != null){
                this.failed = true;
            }
            else
            {
                this.receivedActivities = activities;
            }
            waitSem.release();   
        }
    }


    
}
