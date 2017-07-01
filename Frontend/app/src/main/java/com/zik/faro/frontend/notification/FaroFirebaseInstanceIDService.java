package com.zik.faro.frontend.notification;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FaroFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseInstIdService";
    public static final String  TOKEN_BROADCAST = "myFCMTokenBroadcast";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        /* Don't send the token to server from here. Since the token could be received here even
         * before the user logs into the app. What can be done here is to setup a broadcast message
         * to AppLanding Page to handle this.
         */

    }
};