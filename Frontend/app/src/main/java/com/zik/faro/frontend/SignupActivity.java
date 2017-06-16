package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.google.firebase.iid.FirebaseInstanceId;
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
import java.util.Date;

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
    private Context mContext = null;
    static FaroCache faroCache;
    static FaroUserContext faroUserContext = FaroUserContext.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mContext = this;

        //Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(SignupActivity.this);

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

        if (Strings.isNullOrEmpty(password)) {
            isValid = false;
            passwordTextBox.setError("Enter Password");
        }

        if (Strings.isNullOrEmpty(comfirmPasswd)) {
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

    private void signUpUser () {
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the activity_signup data and validate
                final String name = nameTextBox.getText().toString();
                final String email = emailTextBox.getText().toString();
                String password = passwordTextBox.getText().toString();
                String confirmPassword = confirmPasswordBox.getText().toString();

                if (validate(name, email, password, confirmPassword)) {
                    FaroUser newFaroUser = new FaroUser(email, null, null,
                            null, null, null, null);
                    newFaroUser.setFirstName(name);

                    serviceHandler.getSignupHandler().signup(new BaseFaroRequestCallback<String>() {
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
                                faroUserContext.setEmail(email);

                                // Go to event list page
                                startActivity(appLandingPageIntent);
                                finish();
                            } else {
                                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                                if (error.getCode() == 409) {
                                    Log.i(TAG, "User " + email + " already exists");
                                }
                            }
                        }
                    }, newFaroUser, password);
                }
            }
        };
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(myRunnable);
    }

    private void deleteFirebaseToken () {
        //Delete Firebase token.
        final String oldToken = FirebaseInstanceId.getInstance().getToken();
        if (oldToken != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        String refreshedToken = oldToken;

                        Log.d(TAG, "== Old Token is " + oldToken);

                        long startTime = System.currentTimeMillis();
                        long elapsedTime = 0L;

                        //Retry for 10 secs or until a new token is received
                        while (elapsedTime < 10 * 1000) {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                            refreshedToken = FirebaseInstanceId.getInstance().getToken();
                            if (refreshedToken == null || !refreshedToken.equals(oldToken))
                                break;
                            elapsedTime = (new Date()).getTime() - startTime;
                        }

                        Log.d(TAG, "== Refreshed Token is " + refreshedToken);

                        if (refreshedToken == null) {
                            signUpUser();
                        } else if (!refreshedToken.equals(oldToken)) {
                            //TODO: Make API call to our server and update new token
                            Log.d(TAG, "== Making API call to our server with the firebase token");
                            // if (successfull) {
                                faroUserContext.setFirebaseToken(refreshedToken);
                                faroCache.saveFaroCacheToDisk("firebaseToken", refreshedToken);
                                signUpUser();
                            //} else {
                                //Ask user to retry
                                return;
                            //}
                        } else if (refreshedToken.equals(oldToken)) {
                            // Firebase token was not deleted. User needs to retry login
                            // TODO: Display popUp to user that login is not possible so try again.
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            signUpUser();
        }
    }

    public void onDoneClick(View view) {
        if (faroUserContext.getFirebaseToken() != null){
            //Token was not deleted from our server during logout so try deleting now.
            //TODO: make API call to our server to delete Firebase token.
            Log.d(TAG, "== Making API call to our server to delete the firebase token");
            // If successful then continue else ask user to retry
            //if (successful) {
                faroUserContext.setFirebaseToken(null);
                faroCache.saveFaroCacheToDisk("firebaseToken", null);
                deleteFirebaseToken();
            //} else {
                //Ask User to try again
            //}
        } else {
            deleteFirebaseToken();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }
}
