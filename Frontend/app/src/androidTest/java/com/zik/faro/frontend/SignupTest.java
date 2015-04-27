package com.zik.faro.frontend;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.frontend.data.MinEvent;
import com.zik.faro.frontend.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SignupTest extends ApplicationTestCase<Application> {
    public SignupTest() {
        super(Application.class);
    }

    private static final String baseUrl = "http://10.0.2.2:8080/v1/";

    @LargeTest
    public void testSignup() throws InterruptedException, MalformedURLException {
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        String uuidEmail = UUID.randomUUID().toString()+ "@gmail.com";
        String password = UUID.randomUUID().toString();

        TestSignupCallback callback = new TestSignupCallback(waitSem, 404);
        serviceHandler.getSignupHandler().signup(callback, new FaroUser(uuidEmail), password);
        boolean timeout;
        timeout = !waitSem.tryAcquire(30000, TimeUnit.MILLISECONDS);

        Assert.assertFalse(timeout);
        Assert.assertFalse(callback.failed);
        Assert.assertFalse(callback.unexpectedResponseCode);
        Assert.assertNotNull(callback.token);
        Assert.assertEquals(TokenCache.getTokenCache().getAuthToken(), callback.token);
    }


    final static class TestSignupCallback
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<String> {
        String token;
        TestSignupCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
        }

        @Override
        public void onResponse(String token, HttpError error){
            if(error != null){
                this.failed = true;
                return;
            }
            this.token = token;
            waitSem.release();
        }
    }
}
