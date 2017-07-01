package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Strings;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.faroservice.FaroSignupJob;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.SignupJobResultHandler;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

/**
 * Created by granganathan on 1/24/16.
 */
public class SignupActivity extends Activity {
    private String TAG = "SignupActivity";
    private FaroServiceHandler serviceHandler = null;

    private EditText nameTextBox;
    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordBox;
    private EditText serverIPAddressEditText;

    private Intent appLandingPageIntent;

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
        serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        appLandingPageIntent = new Intent(SignupActivity.this, AppLandingPage.class);
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

        String serverIpAddress = serverIPAddressEditText.getText().toString().trim();
        if (!Strings.isNullOrEmpty(serverIpAddress)) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIpAddress);
        }

        // Create Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        if (validate(name, email, password, confirmPassword)) {
            FaroUser newFaroUser = new FaroUser(email, name, null, null, null, null, null);

            SignupJobResultHandler signupResultHandler = new SignupJobResultHandler<String>() {
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

            FaroExecutionManager.execute(new FaroSignupJob(newFaroUser, password).addResultHandler(this, signupResultHandler));
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }
}
