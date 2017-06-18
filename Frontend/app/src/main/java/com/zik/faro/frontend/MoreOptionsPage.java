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
import com.squareup.okhttp.Request;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MoreOptionsPage extends Fragment {
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    private final static String TAG = "MoreOptions";

    private static final int FIREBASE_DELETE_RETRY_TIMEOUT = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View moreOptionsPageView = inflater.inflate(R.layout.activity_more_options_page, container, false);
        final Button logout = (Button)moreOptionsPageView.findViewById(R.id.logout);
        TextView textView = (TextView) moreOptionsPageView.findViewById(R.id.text);
        textView.setText("Logged in as " + myUserId);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable myRunnable = new Runnable() {
                     @Override
                     public void run() {
                         String currentToken = FirebaseInstanceId.getInstance().getToken();
                         String newToken = null;

                         long startTime = System.currentTimeMillis();
                         long elapsedTime = 0L;

                         // Try to delete token from the Firebase server for 10 secs. Else check if
                         // token still present in the cache during login and delete it then.
                         // Allow login only after the token is deleted.
                         while (elapsedTime < TimeUnit.SECONDS.toMillis(FIREBASE_DELETE_RETRY_TIMEOUT)) {
                             try {
                                 // Delete firebase token
                                 FirebaseInstanceId.getInstance().deleteInstanceId();

                                 newToken = FirebaseInstanceId.getInstance().getToken();
                                 if (newToken == null || !newToken.equals(currentToken)){
                                     break;
                                 }
                                 elapsedTime = System.currentTimeMillis() - startTime;

                             } catch (IOException e) {
                                 Log.e(TAG, "Could not delete Firebase token ", e);
                             }
                         }

                         if (!currentToken.equals(newToken)) {
                             // Delete token from appserver
                             FaroServiceHandler.getFaroServiceHandler().getProfileHandler()
                                     .removeRegistrationToken(new BaseFaroRequestCallback<String>() {
                                         @Override
                                         public void onFailure(Request request, IOException ex) {
                                             Log.e(TAG, "Failed to remove token from faro app server", ex);
                                             // Dont proceed with logout
                                         }

                                         @Override
                                         public void onResponse(String s, HttpError error) {
                                             new Handler(getActivity().getMainLooper()).post(
                                                 new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             // proceed with logging out of the app
                                                             logout();
                                                         }
                                                     });
                                         }
                                     }, currentToken);
                         } else {
                             Log.d(TAG, "== Failed to delete the Firebase token.");
                             // Dont proceed with logout
                         }
                     }
                 };
            }
        });

        return moreOptionsPageView;
    }

    private void logout() {
        // Log out of facebook
        LoginManager.getInstance().logOut();

        // Clear Faro auth token
        TokenCache.getTokenCache().deleteToken();

        // Clear all App related info here
        eventListHandler.clearListAndMapOnLogout();
        userFriendListHandler.clearFriendListAndMap();

        // Go to LoginActivity page
        Intent loginActivity = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginActivity);
        getActivity().finish();

    }
}
