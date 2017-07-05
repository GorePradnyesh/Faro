package com.zik.faro.frontend.faroservice.auth;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.auth.facebook.FacebookLoginJob;
import com.zik.faro.frontend.faroservice.auth.facebook.FacebookSignupJob;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by gaurav on 6/24/17.
 */

public abstract class FaroBaseSignInJob implements FaroSignInJob {
    private String TAG = "FaroBaseSignInJob";
    private Activity activity;
    private SignInJobResultHandler resultHandler;
    protected String email;
    protected FaroServiceHandler faroServiceHandler;

    public FaroBaseSignInJob(String email) {
        this.email = email;
        faroServiceHandler = FaroServiceHandler.getFaroServiceHandler();
    }

    public FaroBaseSignInJob() {
        faroServiceHandler = FaroServiceHandler.getFaroServiceHandler();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public FaroBaseSignInJob addResultHandler(Activity activity, SignInJobResultHandler resultHandler) {
        this.activity = activity;
        this.resultHandler = resultHandler;
        return this;
    }

    @Override
    public void run() {
        String firebaseNotificationToken = FirebaseInstanceId.getInstance().getToken();

        if (Strings.isNullOrEmpty(firebaseNotificationToken)) {
            Log.e(TAG, "firebase notification token is not present. Cant allow user to proceed.");

            if (this instanceof FacebookLoginJob || this instanceof FacebookSignupJob) {

            }

            handleResultOnUi(new SignInJobResult(email, new Exception("firebase notification token is not present")));
            return;
        }

        try {
            // Sign in the user. This could be logging in or signing up the user
            OkHttpResponse<String> signInResponse = signIn();

            if (signInResponse.isSuccessful()) {
                String token = signInResponse.getResponseObject();
                Log.i(TAG, MessageFormat.format("Token from signInResponse = {0}", token));

                // Update the firebase notification token on the app server
                try {
                    OkHttpResponse<Void> response = faroServiceHandler.getProfileHandler()
                            .addRegistrationToken(firebaseNotificationToken);

                    if (response.isSuccessful()) {
                        Log.i(TAG, MessageFormat.format("Added firebase token {0} to faro app server", firebaseNotificationToken));
                        handleResultOnUi(new SignInJobResult(email, token));
                    } else {
                        Log.i(TAG, MessageFormat.format("Failed to add firebase token {0} on faro app server. httpErrpr = {1}",
                                firebaseNotificationToken, response.getHttpError()));
                        undoSignIn();
                        handleResultOnUi(new SignInJobResult(email, response.getHttpError()));
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to issue request to add token to faro app server", ex);
                    undoSignIn();
                    handleResultOnUi(new SignInJobResult(email, ex));
                }
            } else {
                handleResultOnUi(new SignInJobResult(email, signInResponse.getHttpError()));
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to send sign in request", e);
            if (e instanceof FirebaseAuthException) {
                handleResultOnUi(new SignInJobResult(email, (FirebaseAuthException) e));
            } else {
                handleResultOnUi(new SignInJobResult(email, e));
            }
        }
    }

    protected void handleResultOnUi(final SignInJobResult response) {
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
