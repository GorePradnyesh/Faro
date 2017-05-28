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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.okhttp.Request;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;

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
        final EditText serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        appLandingPageIntent = new Intent(SignupActivity.this, AppLandingPage.class);

        serverIPAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!serverIPAddressEditText.getText().toString().trim().isEmpty()) {
                    ((FaroApplication)getApplication()).overRideAppServerIp(serverIPAddressEditText.getText().toString().trim());
                }
            }
        });

        // First create facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        // Setup the facebook signup/connect with button
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.fb_signup_button);
        fbLoginButton.setReadPermissions("email", "public_profile");
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
            confirmPasswordBox.setError(null);
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

        if(validate(name, email, password, confirmPassword)) {
            FaroUser newFaroUser = new FaroUser(email, null, null,
            null, null, null, null);
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

                // Go to event list page
                startActivity(appLandingPageIntent);
                finish();
            } else {
                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                if (error.getCode() == 409) {
                    Log.i(TAG, "User " + firebaseUser.getEmail() + " already exists");

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
