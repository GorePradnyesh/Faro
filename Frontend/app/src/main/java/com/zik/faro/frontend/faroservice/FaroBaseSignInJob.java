package com.zik.faro.frontend.faroservice;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;

/**
 * Created by gaurav on 6/24/17.
 */

public abstract class FaroBaseSignInJob implements FaroSignInJob {
    private String TAG = "FaroBaseSignInJob";
    private Activity activity;
    private SignupJobResultHandler resultHandler;
    protected String email;
    protected FaroServiceHandler faroServiceHandler;

    public FaroBaseSignInJob(String email) {
        this.email = email;
        faroServiceHandler = FaroServiceHandler.getFaroServiceHandler();
    }

    public FaroBaseSignInJob addResultHandler(Activity activity, SignupJobResultHandler resultHandler) {
        this.activity = activity;
        this.resultHandler = resultHandler;
        return this;
    }

    @Override
    public void run() {
        String firebaseNotificationToken = FirebaseInstanceId.getInstance().getToken();

        if (Strings.isNullOrEmpty(firebaseNotificationToken)) {
            Log.e(TAG, "firebase notification token is not present. Cant allow user to proceed.");
            handleResultOnUi(new OkHttpResponse<String>(null, null));
            return;
        }

        try {
            OkHttpResponse<String> signInResponse = signIn();

            if (signInResponse.isSuccessful()) {
                String token = signInResponse.getResponseObject();
                Log.i(TAG, "token from signInResponse = " + token);

                // Update the firebase notification token on the app server
                try {
                    OkHttpResponse<Void> response = faroServiceHandler.getProfileHandler()
                            .addRegistrationToken(firebaseNotificationToken);

                    if (response.isSuccessful()) {
                        Log.i(TAG, MessageFormat.format("Added firebase token {0} to faro app server", firebaseNotificationToken));
                        handleResultOnUi(new OkHttpResponse<String>(token, null));
                    } else {
                        Log.i(TAG, MessageFormat.format("Failed to add firebase token {0} on faro app server. httpErrpr = {1}",
                                firebaseNotificationToken, response.getHttpError()));
                        undoSignIn();
                        handleResultOnUi(new OkHttpResponse<String>(null, null));
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to issue request to add token to faro app server", ex);
                    undoSignIn();
                    handleResultOnUi(new OkHttpResponse<String>(null, null));
                }
            } else {
                HttpError httpError = signInResponse.getHttpError();
                Log.i(TAG, "code = " + httpError.getCode() + ", message = " + httpError.getCode());

                if (httpError.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Invalid login
                    Log.i(TAG, "username/password not valid");
                }

                handleResultOnUi(new OkHttpResponse<String>(null, httpError));
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to send sign in request", e);
            handleResultOnUi(new OkHttpResponse<String>(null, null));
        }
    }

    protected void handleResultOnUi(final OkHttpResponse<String> response) {
        if (resultHandler != null) {

            new Handler(activity.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    resultHandler.handleResponse(response);
                }
            });
        }
    }

    private void undoSignIn() {
        TokenCache.getTokenCache().deleteToken();
        FaroUserContext.getInstance().setEmail(null);
        FaroCache.getFaroUserContextCache().saveFaroCacheToDisk("email", null);
    }
}
