package com.zik.faro.frontend;

import android.app.Application;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.Request;
import com.zik.faro.frontend.data.DateOffset;
import com.zik.faro.frontend.data.EventCreateData;
import com.zik.faro.frontend.data.MinUser;
import com.zik.faro.frontend.data.ObjectStatus;
import com.zik.faro.frontend.data.Poll;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.spec.FriendsHandler;

import junit.framework.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FriendsApiTest extends ApiBaseTest{
    public FriendsApiTest()
    {
        super(Application.class);
    }


    @LargeTest
    public void testInviteFriends() throws InterruptedException, MalformedURLException {
        // Sign up user, so that the token cache is populated
        final Semaphore waitSem = new Semaphore(0);
        FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        
        String uuidEmail1 = UUID.randomUUID().toString() + "@gmail.com";
        String password1 = UUID.randomUUID().toString();
        
        String uuidEmail2 = UUID.randomUUID().toString() + "@gmail.com";
        String password2 = UUID.randomUUID().toString();
        
        String uuidEmail3 = UUID.randomUUID().toString() + "@gmail.com";
        String password3 = UUID.randomUUID().toString();

        getTokenForNewUser(uuidEmail1, password1);
        signUpUser(uuidEmail2, password2);
        signUpUser(uuidEmail3, password3);
        boolean timeout;
        
        TestOKActionCallbackHandler inviteUser = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getFriendsHandler().inviteFriend(inviteUser, uuidEmail2);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(inviteUser.failed);
        Assert.assertFalse(inviteUser.unexpectedResponseCode);

        // get and verify that friend has been added
        TestGetFriendsCallback getFriendsCallback = new TestGetFriendsCallback(waitSem, 200);
        serviceHandler.getFriendsHandler().getFriends(getFriendsCallback);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getFriendsCallback.failed);
        Assert.assertFalse(getFriendsCallback.unexpectedResponseCode);
        Assert.assertTrue(getFriendsCallback.friends.size() == 1);
        
        // invite another friend
        inviteUser = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getFriendsHandler().inviteFriend(inviteUser, uuidEmail3);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(inviteUser.failed);
        Assert.assertFalse(inviteUser.unexpectedResponseCode);

        // get and verify that friend has been added
        getFriendsCallback = new TestGetFriendsCallback(waitSem, 200);
        serviceHandler.getFriendsHandler().getFriends(getFriendsCallback);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getFriendsCallback.failed);
        Assert.assertFalse(getFriendsCallback.unexpectedResponseCode);
        Assert.assertTrue(getFriendsCallback.friends.size() == 2);
    
        // unfriend user
        TestOKActionCallbackHandler unfriendCallback = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getFriendsHandler().unFriend(unfriendCallback, uuidEmail2);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(unfriendCallback.failed);
        Assert.assertFalse(unfriendCallback.unexpectedResponseCode);

        
        // get list and verify that the friend has been removed
        getFriendsCallback = new TestGetFriendsCallback(waitSem, 200);
        serviceHandler.getFriendsHandler().getFriends(getFriendsCallback);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getFriendsCallback.failed);
        Assert.assertFalse(getFriendsCallback.unexpectedResponseCode);
        Assert.assertTrue(getFriendsCallback.friends.size() == 1);

        // invite the same friend and make sure its a no-op
        inviteUser = new TestOKActionCallbackHandler(waitSem, 200);
        serviceHandler.getFriendsHandler().inviteFriend(inviteUser, uuidEmail3);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(inviteUser.failed);
        Assert.assertFalse(inviteUser.unexpectedResponseCode);

        getFriendsCallback = new TestGetFriendsCallback(waitSem, 200);
        serviceHandler.getFriendsHandler().getFriends(getFriendsCallback);
        timeout = !waitSem.tryAcquire(testTimeout, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeout);
        Assert.assertFalse(getFriendsCallback.failed);
        Assert.assertFalse(getFriendsCallback.unexpectedResponseCode);
        Assert.assertTrue(getFriendsCallback.friends.size() == 1);
    }


    static class TestGetFriendsCallback extends Utils.BaseTestCallbackHandler implements BaseFaroRequestCallback<List<MinUser>> {
        public List<MinUser> friends;
        TestGetFriendsCallback(Semaphore semaphore, int expectedCode) {
            super(semaphore, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            waitSem.release();
            this.failed = true;
        }

        @Override
        public void onResponse(List<MinUser> minUsers, HttpError error) {
            if(error != null){
                this.failed = true;
                if(error.code != expectedCode)
                {
                    this.unexpectedResponseCode = true;
                }
            }
            else {
                this.friends = minUsers;
            }
            waitSem.release();
        }
    }
}
