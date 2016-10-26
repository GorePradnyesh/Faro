package com.zik.faro.frontend;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.squareup.okhttp.Request;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by granganathan on 1/24/16.
 */
public class SignupActivity extends Activity {
    private String TAG = "SignupActivity";
    private static FaroServiceHandler serviceHandler = null;
    protected static final String baseUrl = "http://192.168.1.10:8080/v1/";
    private String baseUrl2 = "http://192.168.1.10:8080/v1/";

    private EditText nameTextBox;
    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordBox;

    private Intent AppLandingPageIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind the data fields with the corresponding View components
        nameTextBox = (EditText)findViewById(R.id.signupName);
        emailTextBox = (EditText)findViewById(R.id.signupEmail);
        passwordTextBox = (EditText)findViewById(R.id.signupPassword);
        confirmPasswordBox = (EditText)findViewById(R.id.confirmPassword);
        final EditText serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        AppLandingPageIntent = new Intent(SignupActivity.this, AppLandingPage.class);

        serverIPAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (serverIPAddressEditText.getText().toString().trim().isEmpty()){
                    baseUrl2 = "http://192.168.1.2:8080/v1/";
                }else{
                    baseUrl2 = "http://" + serverIPAddressEditText.getText().toString() + ":8080/v1/";
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

            // Create Faro service handler
            try {
                serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl2));
            } catch (MalformedURLException e) {
                Log.e(TAG, "failed to obtain servicehandler", e);
            }

            serviceHandler.getSignupHandler().signup(new BaseFaroRequestCallback<String>() {
                @Override
                public void onFailure(Request request, IOException ex) {
                    Log.i(TAG, "failed to send signup request");
                }

                @Override
                public void onResponse(String token, HttpError error) {
                    Log.i(TAG, "signup response, token = " + token);
                    if (error == null ) {
                        Log.i(TAG, "token = " + token);
                        TokenCache.getTokenCache().setToken(token);
                        FaroUserContext faroUserContext = FaroUserContext.getInstance();
                        faroUserContext.setEmail(email);

                        // Go to event list page
                        AppLandingPageIntent.putExtra("baseUrl", baseUrl2);
                        startActivity(AppLandingPageIntent);
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }
}
