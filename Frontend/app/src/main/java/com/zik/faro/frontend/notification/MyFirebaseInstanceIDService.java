package com.zik.faro.frontend.notification;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
    public static final String  TOKEN_BROADCAST = "myFCMTokenBroadcast";
    static FaroCache faroCache;
    static FaroUserContext faroUserContext = FaroUserContext.getInstance();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {

        //Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(MyFirebaseInstanceIDService.this);

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "== Refreshed token: " + refreshedToken);

        if (faroUserContext.getEmail() != null) {
            // This condition is true only if there is a Faro user logged in at the time when the
            // token was refreshed. So we need to make an API call to update he user's Firebase
            // token on our server.
            //TODO: make API call to our server to update the user's Firebase token.
            Log.d(TAG, "== Making API call to our server with the firebase token");
            //if (successful) {
                faroUserContext.setFirebaseToken(refreshedToken);
                faroCache.saveFaroCacheToDisk("firebaseToken", refreshedToken);
            //} else {
                // Next time when we come to Applanding page it will be retired
            //}
        }
    }
    // [END refresh_token]
};