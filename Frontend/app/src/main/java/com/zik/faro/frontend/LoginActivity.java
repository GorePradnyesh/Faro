package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.zik.faro.frontend.faroservice.FaroLoginJob;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.SignupJobResultHandler;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.text.MessageFormat;

public class LoginActivity extends Activity {
    private String TAG = "LoginActivity";

    private FaroServiceHandler serviceHandler;

    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText serverIPAddressEditText;

    static FaroUserContext faroUserContext = FaroUserContext.getInstance();

    static FaroCache faroCache;

    private Intent appLandingPageIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind the data fields with the corresponding View components
        emailTextBox = (EditText)findViewById(R.id.TFEmail);
        passwordTextBox = (EditText)findViewById(R.id.TFPassword);

        serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        appLandingPageIntent = new Intent(LoginActivity.this, AppLandingPage.class);

        // Setup token cache
        TokenCache tokenCache = TokenCache.getOrCreateTokenCache(LoginActivity.this);

        // Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(LoginActivity.this);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        String token = null;
        try {
            token = tokenCache.getToken();
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
            Log.i(TAG, "token = " + token);
        } catch (Exception e) {
            Log.i(TAG, "Could not obtain valid token");
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
        final String password = passwordTextBox.getText().toString();

        Log.i(TAG, MessageFormat.format("username :{0} password :{1}", email, password));

        String serverIpAddress = serverIPAddressEditText.getText().toString().trim();
        if (!Strings.isNullOrEmpty(serverIpAddress)) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIpAddress);
        }

        Log.i(TAG, "serverIP = " + ((FaroApplication)getApplication()).getAppServerIp());

        // Create Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        if (validate(email, password)) {
            SignupJobResultHandler<String> signupResultHandler = new SignupJobResultHandler<String>() {

                @Override
                public void handleResponse(OkHttpResponse<String> response) {
                    // Go to event list page
                    if (response.isSuccessful() && response.getResponseObject() != null) {
                        startActivity(appLandingPageIntent);
                        finish();
                    }

                    // TODO: Show popup with appropriate message if response is not successfull
                }
            };

            FaroExecutionManager.execute(new FaroLoginJob(email, password).addResultHandler(this, signupResultHandler));
        }
    }

    private void undoLogin() {
        TokenCache.getTokenCache().deleteToken();
        FaroUserContext.getInstance().setEmail(null);
        FaroCache.getFaroUserContextCache().saveFaroCacheToDisk("email", null);
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
