package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.MessageFormat;

public class LoginActivity extends Activity {
    private String TAG = "LoginActivity";

    protected static final String baseUrl = "http://10.0.2.2:8080/v1/";
    private static FaroServiceHandler serviceHandler;

    private EditText emailTextBox;
    private EditText passwordTextBox;

    static FaroUserContext faroUserContext = FaroUserContext.getInstance();

    static FaroCache faroCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);

        // Setup token cache
        TokenCache tokenCache = TokenCache.getOrCreateTokenCache(LoginActivity.this);

        //Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(LoginActivity.this);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        String token = null;
        try {
            token = tokenCache.getToken();
            if (token != null) {
                String email = faroCache.loadFaroCacheFromDisk("email");
                if (email != null){
                    faroUserContext.setEmail(email);
                    startActivity(new Intent(LoginActivity.this, AppLandingPage.class));
                    finish();
                }else{
                    Log.e(TAG, "email not found");
                }
            }else{
                Log.i(TAG, "Token not found");
            }
            Log.i(TAG, "token = " + token);
        } catch (Exception e) {
            Log.i(TAG, "Could not obtain valid token");
        }

        setContentView(R.layout.activity_login);

        // Bind the data fields with the corresponding View components
        emailTextBox = (EditText)findViewById(R.id.TFEmail);
        passwordTextBox = (EditText)findViewById(R.id.TFPassword);

        // Create Faro service handler
        try {
            Log.i(TAG, "baseUrl : " +  baseUrl);
            serviceHandler = FaroServiceHandler.getFaroServiceHandler(new URL(baseUrl));
        } catch (MalformedURLException e) {
            Log.e(TAG, "failed to obtain servicehandler", e);
        }
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

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Buld config is debug. Skip authentication.");
            // Go to event list page
            startActivity(new Intent(LoginActivity.this, AppLandingPage.class));
        }

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
                        startActivity(new Intent(LoginActivity.this, AppLandingPage.class));
                        finish();
                    } else {
                        Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        // Invalid login
                        if (error.getCode() == 401) {
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
