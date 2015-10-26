package com.zik.faro.frontend;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.squareup.okhttp.Request;
import data.user.FaroUser;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ProfileApiTest extends ApplicationTestCase<Application> {
    public ProfileApiTest() {
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
        return callback.token;
    }
    
    @LargeTest
    public void testCreateGetProfile() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail, password);
        boolean timeout;
        
        String uuidFName = UUID.randomUUID().toString();
        String uuidMName = UUID.randomUUID().toString();
        String uuidLName = UUID.randomUUID().toString();
        String uuidExpense = UUID.randomUUID().toString();
        String uuidTelephone = UUID.randomUUID().toString();
        
        FaroUser faroUser = new FaroUser(uuidEmail, uuidFName, uuidMName, uuidLName, uuidExpense, uuidTelephone, null);

        TestCreateProfileCallbackHandler callback = new TestCreateProfileCallbackHandler(waitSem, 200);
        serviceHandler.getProfileHandler().createProfile(callback, faroUser);         
        timeout = !waitSem.tryAcquire(3000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(callback.failed);

        TestGetProfileCallbackHandler getCallback = new TestGetProfileCallbackHandler(waitSem, 200);
        serviceHandler.getProfileHandler().getProfile(getCallback, uuidEmail);
        timeout = !waitSem.tryAcquire(3000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getCallback.failed);
        Assert.assertNotNull(getCallback.receivedFaroUser);
        Assert.assertEquals(getCallback.receivedFaroUser.getFirstName(), uuidFName);
        Assert.assertEquals(getCallback.receivedFaroUser.getLastName(), uuidLName);
        Assert.assertEquals(getCallback.receivedFaroUser.getMiddleName(), uuidMName);
        Assert.assertEquals(getCallback.receivedFaroUser.getExternalExpenseID(), uuidExpense);
        Assert.assertEquals(getCallback.receivedFaroUser.getTelephone(), uuidTelephone);
    }

    final static class TestGetProfileCallbackHandler
            extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<FaroUser> {
        public FaroUser receivedFaroUser;
        TestGetProfileCallbackHandler(final Semaphore sem, int expectedCode){
            super(sem, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            failed = true;
            Log.v("Get Profile", "Failed");
            waitSem.release();
        }

        @Override
        public void onResponse(FaroUser response, HttpError error){
            if(response!= null){
                this.receivedFaroUser = response;
            }else{
                failed = true;
            }
            waitSem.release();
        }
    }

    final static class TestCreateProfileCallbackHandler
            extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<String> {
        public String receivedEvent;
        TestCreateProfileCallbackHandler(final Semaphore sem, int expectedCode){
            super(sem, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.v("Create Profile", "Failed");
            failed = true;
            waitSem.release();
        }

        @Override
        public void onResponse(String response, HttpError error){
            if(response!= null){
                this.receivedEvent = response;
            }else{
                failed = true;
            }
            Log.v("Create Profile", "Success");
            waitSem.release();
        }
    }
}
