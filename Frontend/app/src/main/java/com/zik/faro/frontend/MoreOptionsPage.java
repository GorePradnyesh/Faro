package com.zik.faro.frontend;

import android.app.NotificationManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.common.base.Strings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class MoreOptionsPage extends Fragment {
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();
    private LinearLayout progressBarLayout;
    private LinearLayout moreOptionsLayout;

    private final static String TAG = "MoreOptions";

    private static final int FIREBASE_DELETE_RETRY_TIMEOUT = 10;

    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View moreOptionsPageView = inflater.inflate(R.layout.activity_more_options_page, container, false);

        progressBarLayout = (LinearLayout) moreOptionsPageView.findViewById(R.id.moreOptionsPageProgressBarLayout);
        moreOptionsLayout = (LinearLayout) moreOptionsPageView.findViewById(R.id.moreOptionsPageLayout);

        progressBarLayout.setVisibility(View.GONE);
        mContext = getActivity();

        final Button logout = (Button)moreOptionsPageView.findViewById(R.id.logout);
        TextView textView = (TextView) moreOptionsPageView.findViewById(R.id.text);
        textView.setText("Logged in as " + myUserId);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 showProgressBar();
                 FaroExecutionManager.execute(new LogoutJob());
            }
        });

        return moreOptionsPageView;
    }

    private class LogoutJob implements Runnable {

        @Override
        public void run() {
            String currentToken = FirebaseInstanceId.getInstance().getToken();
            if (!Strings.isNullOrEmpty(currentToken)) {
                deleteFirebaseTokenAndLogout(currentToken);
            } else {
                // proceed with logging out of the app
                handleLogoutSuccessOnUi();
            }
        }

        private void deleteFirebaseTokenAndLogout(String currentToken) {
            // Try to delete token from the Firebase server for 10 secs. Else check if
            // token still present in the cache during login and delete it then.
            // Allow login only after the token is deleted.
            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            String newToken = null;

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
                try {
                    OkHttpResponse<Void> response = FaroServiceHandler.getFaroServiceHandler().getProfileHandler()
                            .removeRegistrationToken(currentToken);

                    if (response.isSuccessful()) {
                        Log.e(TAG, MessageFormat.format("Removed firebase token {0} from faro app server", currentToken));
                    } else {
                        Log.e(TAG, MessageFormat.format("Failed to remove token from faro app server. response code = {0}",
                                response.getHttpError().getCode()));
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to issue request to remove token from faro app server", ex);
                }

                // proceed with logging out of the app even if token is not removed from app server.
                // App server will realize later, on its own, that the token is invalid when it uses it
                handleLogoutSuccessOnUi();
            } else {
                // Dont proceed with logout
                Log.d(TAG, "Failed to delete the Firebase token. Cannot logout the user");
                handleLogoutFailureOnUi();
            }
        }

        private void handleLogoutFailureOnUi() {
            new Handler(getActivity().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    logoutFailure();
                }
            });
        }

        private void handleLogoutSuccessOnUi() {
            new Handler(getActivity().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    logoutSuccess();
                }
            });
        }
    }

    private void showMoreOptions() {
        progressBarLayout.setVisibility(View.GONE);
        moreOptionsLayout.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        moreOptionsLayout.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void logoutSuccess() {
        // Log out of facebook
        LoginManager.getInstance().logOut();

        // Clear Faro auth token
        TokenCache.getTokenCache().deleteToken();

        // Clear all App related info here
        eventListHandler.clearListAndMap();
        userFriendListHandler.clearFriendListAndMap();

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        // Go to LoginActivity page
        Intent loginActivity = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginActivity);
        getActivity().finish();
    }

    private void logoutFailure() {
        showMoreOptions();
        // TODO : Also show popup of logout failure
    }
}
