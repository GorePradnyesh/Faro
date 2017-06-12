package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.internal.http.HttpConnection;
import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created by granganathan on 1/24/16.
 */
public class SignupActivity extends Activity {
    private String TAG = "SignupActivity";
    private static FaroServiceHandler serviceHandler = null;

    private EditText nameTextBox;
    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordBox;
    private EditText serverIPAddressEditText;

    private LinearLayout signupActivityProgressBarLayout;
    private RelativeLayout signupActivityDetailsLayout;

    private Intent appLandingPageIntent;

    private CallbackManager callbackManager;   // Facebook login callbackmanager

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Create Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        // Bind the data fields with the corresponding View components
        nameTextBox = (EditText)findViewById(R.id.signupName);
        emailTextBox = (EditText)findViewById(R.id.signupEmail);
        passwordTextBox = (EditText)findViewById(R.id.signupPassword);
        confirmPasswordBox = (EditText)findViewById(R.id.confirmPassword);
        serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        // Get the progress bar and login details layouts
        signupActivityProgressBarLayout = (LinearLayout) findViewById(R.id.signupActivityProgressBarLayout);
        signupActivityDetailsLayout = (RelativeLayout) findViewById(R.id.signupActivityDetailsLayout);

        // Set visibility to gone on the progrss bar layout
        signupActivityProgressBarLayout.setVisibility(View.GONE);

        appLandingPageIntent = new Intent(SignupActivity.this, AppLandingPage.class);

        // First create facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        // Setup the facebook signup/connect with button
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.fb_signup_button);
        fbLoginButton.setReadPermissions("email", "public_profile", "user_photos", "user_friends");
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "fb login button pressed. Bring up the progress bar");
                signupActivityProgressBarLayout.setVisibility(View.VISIBLE);
                signupActivityDetailsLayout.setVisibility(View.GONE);
            }
        });
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "fbLogin successful. loginResult =  " + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "fbLogin cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "fbLogin error", error);
            }
        });

        // Get Firebase Auth instance
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        // Get Firebase AuthCredential object from the Fb access token
        AuthCredential authCredential = FacebookAuthProvider.getCredential(token.getToken());

        // Now proceed with signup workflow

        // Sign in the user using the Firebase credential.
        // This method will create an account in Firebase for the user if it does not already exist
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "firebaseAuth signInWithCredential complete");
                            signupWithFirebaseToken(task.getResult().getUser());
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Log.i(TAG, "user already exists ");
                        }
                    }
                });
    }

    private void signupWithFirebaseToken(final FirebaseUser firebaseUser) {
        firebaseUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            final String firebaseIdToken = task.getResult().getToken();
                            Log.i(TAG, "firebaseIdToken = " + firebaseIdToken);

                            FaroUser newFaroUser = new FaroUser(firebaseUser.getEmail(), null, null,
                                    null, null, null, null);
                            newFaroUser.setFirstName(firebaseUser.getDisplayName());
                            FirebaseUserSignupCallback firebaseUserSignupCallback = new FirebaseUserSignupCallback(firebaseUser);
                            serviceHandler.getSignupHandler().signup(firebaseUserSignupCallback, newFaroUser, null, firebaseIdToken);
                        }
                    }

                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Validate the Input data and set error on the
     * components that have invalid data
     *
     * @param name
     * @param email
     * @param password
     * @param comfirmPasswd
     * @return
     *      true if all inputs are valid
     */
    private boolean validate(String name, String email, String password, String comfirmPasswd) {
        boolean isValid = true;

        if (Strings.isNullOrEmpty(name)) {
            isValid = false;
            nameTextBox.setError("Enter Name");
        }

        if (Strings.isNullOrEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false;
            emailTextBox.setError("Enter valid Email address");
        }

        if (passwordTextBox.getVisibility() == View.VISIBLE && Strings.isNullOrEmpty(password)) {
            isValid = false;
            passwordTextBox.setError("Enter Password");
        }

        if (confirmPasswordBox.getVisibility() == View.VISIBLE && Strings.isNullOrEmpty(comfirmPasswd)) {
            isValid = false;
            confirmPasswordBox.setError("Enter Password again");
        }

        if (!Strings.isNullOrEmpty(password) &&
                !Strings.isNullOrEmpty(comfirmPasswd) &&
                !password.equals(comfirmPasswd)) {
            isValid = false;
            confirmPasswordBox.setError("Passwords do not match");
        }

        return isValid;
    }

    public void onDoneClick(View view) {
        // Get the activity_signup data and validate
        final String name = nameTextBox.getText().toString();
        final String email = emailTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();
        String confirmPassword = confirmPasswordBox.getText().toString();

        if (!serverIPAddressEditText.getText().toString().trim().isEmpty()) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIPAddressEditText.getText().toString().trim());
        }

        if (validate(name, email, password, confirmPassword)) {
            FaroUser newFaroUser = new FaroUser(email, null, null, null, null, null, null);
            newFaroUser.setFirstName(name);

            FarouserSignupCallback farouserSignupCallback = new FarouserSignupCallback(newFaroUser);
            serviceHandler.getSignupHandler().signup(farouserSignupCallback, newFaroUser, password);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }

    private class FarouserSignupCallback implements BaseFaroRequestCallback<String> {
        private FaroUser faroUser;

        public FarouserSignupCallback(FaroUser faroUser) {
            this.faroUser = faroUser;
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            Log.i(TAG, "failed to send signup request");
        }

        @Override
        public void onResponse(String token, HttpError error) {
            Log.i(TAG, "signup response, token = " + token);
            if (error == null) {
                Log.i(TAG, "token = " + token);
                TokenCache.getTokenCache().setToken(token);
                FaroUserContext faroUserContext = FaroUserContext.getInstance();
                faroUserContext.setEmail(faroUser.getEmail());

                // Go to event list page
                startActivity(appLandingPageIntent);
                finish();
            } else {
                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                if (error.getCode() == 409) {
                    Log.i(TAG, "User " + faroUser.getEmail() + " already exists");
                }
            }
        }
    }

    private class FirebaseUserSignupCallback implements BaseFaroRequestCallback<String> {
        private FirebaseUser firebaseUser;

        public FirebaseUserSignupCallback(FirebaseUser firebaseUser) {
            this.firebaseUser = firebaseUser;
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            Log.i(TAG, "failed to send signup request");
        }

        @Override
        public void onResponse(String token, HttpError error) {
            Log.i(TAG, "signup response, token = " + token);
            if (error == null) {
                Log.i(TAG, "token = " + token);
                TokenCache.getTokenCache().setToken(token);
                FaroUserContext faroUserContext = FaroUserContext.getInstance();
                faroUserContext.setEmail(firebaseUser.getEmail());

                // Find all Facebook friends who are also Faro users and set up friend relation with them
                FbGraphApiService fbGraphApiService = new FbGraphApiService();
                fbGraphApiService.findFacebookFriends(new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        // handle the result
                        if (response.getError() == null) {
                            JSONObject jsonObject = response.getJSONObject();
                            Log.i(TAG, MessageFormat.format("jsonObject = {0}", jsonObject));
                            try {
                                JSONArray friendsArray = jsonObject.getJSONArray("data");
                                List<String> friendIds = Lists.newArrayList();
                                if (friendsArray != null) {
                                    for (int i = 0; i < friendsArray.length(); i++) {
                                        JSONObject friend = friendsArray.getJSONObject(i);
                                        String firstName = friend.getString("first_name");
                                        String lastName = friend.getString("last_name");
                                        String id = friend.getString("id");
                                        friendIds.add(id);

                                        Log.i(TAG, MessageFormat.format("friend first_name = {0}, last_name = {1}, id = {2}",
                                                firstName, lastName, id));
                                    }

                                    // Setup friend relation with all of these friends
                                    if (!friendIds.isEmpty()) {
                                        serviceHandler.getFriendsHandler().inviteFacebookFriends(new BaseFaroRequestCallback<List<MinUser>>() {
                                            @Override
                                            public void onFailure(Request request, IOException ex) {
                                                Log.e(TAG, "failed to establish friend relation with facebook friends");
                                            }

                                            @Override
                                            public void onResponse(List<MinUser> minUsers, HttpError error) {
                                                Log.i(TAG, "successfully established friend relation with facebook friends");
                                            }
                                        }, friendIds);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error processing response for friends list", e);
                            }
                        }
                    }
                });

                // Go to event list page
                startActivity(appLandingPageIntent);
                finish();
            } else {
                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                if (error.getCode() == HttpURLConnection.HTTP_CONFLICT) {
                    Log.i(TAG, "User " + firebaseUser.getEmail() + " already exists");

                    // Log out of Facebook
                    LoginManager.getInstance().logOut();

                    // Prompt to sign in using username back on the LoginActivity Page
                    Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                    loginIntent.putExtra("email", firebaseUser.getEmail());
                    startActivity(loginIntent);
                    finish();
                }
            }
        }
    }
}
