package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;
import java.util.Date;

public class MoreOptionsPage extends Fragment{

    static EventListHandler eventListHandler = EventListHandler.getInstance();
    static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();
    String TAG = "MoreOptions";
    private Context mContext;
    static FaroCache faroCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Intent LoginActivity = new Intent(getActivity(), com.zik.faro.frontend.LoginActivity.class);
        View v = inflater.inflate(R.layout.activity_more_options_page, container, false);
        Button logout = (Button)v.findViewById(R.id.logout);
        TextView tv = (TextView) v.findViewById(R.id.text);
        tv.setText("Logged in as " + myUserId);

        //Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(mContext);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear all App related info here
                eventListHandler.clearListAndMapOnLogout();
                userFriendListHandler.clearFriendListAndMap();

                // Reset the faroUserContext
                faroUserContext.setEmail(null);

                // TODO: Send delete token API to our server. If successfull then cacheToken = null;
                Log.d(TAG, "== Making API call to our server to delete the firebase token");
                //if (successfull) {
                    faroUserContext.setFirebaseToken(null);
                    faroCache.saveFaroCacheToDisk("firebaseToken", null);
                //}

                //Delete Firebase token.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String oldToken = FirebaseInstanceId.getInstance().getToken();
                            String refreshedToken = null;

                            long startTime = System.currentTimeMillis();
                            long elapsedTime = 0L;

                            //Try to delete token from the Firebase server for 10 secs. Else check if
                            // token still present in the cache during login and delete it then.
                            // Allow login only after the token is deleted.
                            while (elapsedTime < 10*1000) {
                                FirebaseInstanceId.getInstance().deleteInstanceId();
                                refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                if (refreshedToken == null || !refreshedToken.equals(oldToken)){
                                    break;
                                }
                                elapsedTime = (new Date()).getTime() - startTime;
                            }

                            if (refreshedToken != null && refreshedToken.equals(oldToken)) {
                                Log.d(TAG, "== Failed to delete the Firebase token.");
                            }

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    // Log out of facebook
                                    LoginManager.getInstance().logOut();

                                    TokenCache.getTokenCache().deleteToken();

                                    startActivity(LoginActivity);
                                    getActivity().finish();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        return v;
    }
}
