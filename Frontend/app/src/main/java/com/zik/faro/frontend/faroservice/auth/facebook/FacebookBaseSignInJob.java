package com.zik.faro.frontend.faroservice.auth.facebook;


import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.facebook.FbGraphApiService;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroBaseSignInJob;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import org.json.JSONException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by gaurav on 6/29/17.
 */

public abstract class FacebookBaseSignInJob extends FaroBaseSignInJob implements FacebookSignInJob {
    private String TAG = "FacebookBaseSignInJob";
    private AccessToken facebookAccessToken;
    private boolean addFaroFacebookFriends = true;
    private static final Long FIREBASE_SIGNIN_TIMEOUT_SECS = 15L;

    public FacebookBaseSignInJob(AccessToken facebookAccessToken) {
        super();
        this.facebookAccessToken = facebookAccessToken;
    }

    @Override
    public OkHttpResponse<String> signIn() throws Exception {
        // Get Firebase AuthCredential object from the Fb access token
        AuthCredential authCredential = FacebookAuthProvider.getCredential(facebookAccessToken.getToken());

        // Now proceed with the login workflow
        // Sign in the user using the Firebase credential.
        Task<AuthResult> signInWithCredentialTask = FirebaseAuth.getInstance().signInWithCredential(authCredential);

        try {
            AuthResult authResult = Tasks.await(signInWithCredentialTask, FIREBASE_SIGNIN_TIMEOUT_SECS, TimeUnit.SECONDS);
            FirebaseUser firebaseUser = authResult.getUser();
            setEmail(firebaseUser.getEmail());

            // Get Firebase JWT auth token for the user
            Task<GetTokenResult> getTokenResultTask = firebaseUser.getToken(true);
            GetTokenResult getTokenResult = Tasks.await(getTokenResultTask);

            // Now login to Faro server using the firebase token
            OkHttpResponse<String> response = faroSignIn(firebaseUser, getTokenResult.getToken());
            if (!response.isSuccessful()) {
                // Logout of facebook
                undoFacebookLogin();
            } else if (addFaroFacebookFriends) {
                addFriends();
            }

            return response;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Log.e(TAG, "Failed to sign in via firebase and get firebase JWT auth token", e);
            // Logout of facebook
            undoFacebookLogin();
            throw e;
        } catch (IOException e) {
            Log.e(TAG, "Failed to login to faro app server using firebase JWT auth token", e);
            // Logout of facebook
            undoFacebookLogin();
            throw e;
        }
    }

    private void undoFacebookLogin() {
        LoginManager.getInstance().logOut();
    }

    private void addFriends() {
        // Find all Facebook friends who are also Faro users and set up friend relation with them
        // Do this operation in the background
        final FbGraphApiService fbGraphApiService = new FbGraphApiService();
        fbGraphApiService.findFacebookFriends(new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                // handle the result
                try {
                    List<String> facebookFriendIds = fbGraphApiService.getFriendIdsFromGraphResponse(response);

                    // Setup friend relation with all of these friends
                    if (!facebookFriendIds.isEmpty()) {
                        FaroServiceHandler.getFaroServiceHandler().getFriendsHandler().inviteFacebookFriends(new BaseFaroRequestCallback<List<MinUser>>() {
                            @Override
                            public void onFailure(Request request, IOException ex) {
                                Log.e(TAG, "failed to establish friend relation with facebook friends", ex);
                            }

                            @Override
                            public void onResponse(List<MinUser> minUsers, HttpError error) {
                                if (error != null) {
                                    Log.i(TAG, "successfully established friend relation with facebook friends");
                                } else {
                                    Log.e(TAG, MessageFormat.format("Failed to establish friend relation with facebook friends. error = {0}", error));
                                }
                            }
                        }, facebookFriendIds);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not establish friend relation with facebook friends");
                }
            }
        });
    }

}
