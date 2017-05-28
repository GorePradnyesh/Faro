package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

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
import java.net.HttpURLConnection;
import java.text.MessageFormat;

public class LoginActivity extends Activity {
    private String TAG = "LoginActivity";

    private static FaroServiceHandler serviceHandler;

    private EditText emailTextBox;
    private EditText passwordTextBox;

    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    static FaroCache faroCache;

    private Intent appLandingPageIntent;

    private CallbackManager callbackManager;   // Facebook login callbackmanager
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create the facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        // Create Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        // Bind the data fields with the corresponding View components
        emailTextBox = (EditText)findViewById(R.id.TFEmail);
        passwordTextBox = (EditText)findViewById(R.id.TFPassword);

        final EditText serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        appLandingPageIntent = new Intent(LoginActivity.this, AppLandingPage.class);

        // Setup token cache
        TokenCache tokenCache = TokenCache.getOrCreateTokenCache(LoginActivity.this);

        // Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(LoginActivity.this);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        serverIPAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!serverIPAddressEditText.getText().toString().trim().isEmpty()) {
                    ((FaroApplication)getApplication()).overRideAppServerIp(serverIPAddressEditText.getText().toString().trim());
                }
            }
        });

        // Check if we have arrived on LoginActivity from the SignupActivity because third party signup process
        // determined that the user's account already exists
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !Strings.isNullOrEmpty(bundle.getString("email"))) {
            emailTextBox.setText(bundle.getString("email"));
            emailTextBox.setError("You already have an account with this email. Login by entering the password");
            return;
        }

        String token = null;
        try {
            token = tokenCache.getToken();

            // Check if user is already signed in
            // TODO: Validate the token
            if (token != null) {
                String email = faroCache.loadFaroCacheFromDisk("email");
                if (email != null) {
                    faroUserContext.setEmail(email);
                    startActivity(appLandingPageIntent);
                    finish();
                } else {
                    Log.e(TAG, "email not found");
                }
            } else {
                Log.i(TAG, "Token not found");
            }

            Log.d(TAG, "token = " + token);
        } catch (Exception e) {
            Log.i(TAG, "Could not obtain valid token");
        }

        // If we reach here, it means the user is not signed in.

        // Set up the fblogin button
        // Setup the facebook signup/connect with button
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                            loginWithFirebaseToken(task.getResult().getUser());
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

    private void loginWithFirebaseToken(final FirebaseUser firebaseUser) {
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
                            FirebaseUserLoginCallback firebaseUserLoginCallback = new FirebaseUserLoginCallback(firebaseUser);
                            serviceHandler.getFirebaseLoginHandler().login(firebaseUserLoginCallback, newFaroUser.getEmail(), null, firebaseIdToken);
                        }
                    }

                });
    }

    private class FirebaseUserLoginCallback implements BaseFaroRequestCallback<String> {
        private FirebaseUser firebaseUser;

        public FirebaseUserLoginCallback(FirebaseUser firebaseUser) {
            this.firebaseUser = firebaseUser;
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            Log.i(TAG, "failed to send login request");
        }

        @Override
        public void onResponse(String token, HttpError error) {
            Log.i(TAG, "login response, token = " + token);
            if (error == null) {
                Log.i(TAG, "token = " + token);
                TokenCache.getTokenCache().setToken(token);
                FaroUserContext faroUserContext = FaroUserContext.getInstance();
                faroUserContext.setEmail(firebaseUser.getEmail());
                faroCache.saveFaroCacheToDisk("email", firebaseUser.getEmail());

                // Go to event list page
                startActivity(appLandingPageIntent);
                finish();
            } else {
                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Validate the Input data and set error on the
     * components that have invalid data
     *
     * @param email
     * @param password
     * @return
     *      true if all inputs are valid
     */
    private boolean validate(String email, String password) {
        boolean isValid = true;

        if (Strings.isNullOrEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false;
            emailTextBox.setError("Enter valid Email address");
        }

        if (Strings.isNullOrEmpty(password)) {
            isValid = false;
            passwordTextBox.setError("Enter Password");
        }

        return isValid;
    }

    /**
     * Handle login button click
     * If login is successful, go to the event list page
     * else show dialog box with login error
     * @param view
     */
    public void onLoginClick(View view) {
        Log.i(TAG, "login button clicked");

        final String email = emailTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();

        Log.i(TAG, MessageFormat.format("username :{0} password :{1}", email, password));

        Log.i(TAG, "serverIP = " + ((FaroApplication)getApplication()).getAppServerIp());

        if (validate(email, password)) {
            serviceHandler.getLoginHandler().login(new BaseFaroRequestCallback<String>() {
                @Override
                public void onFailure(Request request, IOException ex) {
                    Log.i(TAG, "failed to send login request");
                }

                @Override
                public void onResponse(String token, HttpError error) {
                    Log.i(TAG, "login response, token = " + token);
                    if (error == null) {
                        faroUserContext.setEmail(email);
                        faroCache.saveFaroCacheToDisk("email", email);
                        // Go to event list page
                        //appLandingPageIntent.putExtra("baseUrl", baseUrl);
                        startActivity(appLandingPageIntent);
                        finish();
                    } else {
                        Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        // Invalid login
                        if (error.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            Log.i(TAG, "username/password not valid");
                        }
                    }
                }
            }, email, password);
        }
    }

    /**
     * Handle signup button click to start
     * the signup activity
     * @param view
     */
    public void onSignupClick(View view) {
        Log.i(TAG, "activity_signup button clicked");
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
