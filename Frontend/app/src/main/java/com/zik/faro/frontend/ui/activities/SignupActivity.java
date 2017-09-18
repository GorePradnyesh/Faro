package com.zik.faro.frontend.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.frontend.FaroApplication;
import com.zik.faro.frontend.faroservice.FaroExecutionManager;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.auth.FaroHttpException;
import com.zik.faro.frontend.faroservice.auth.SignInJobException;
import com.zik.faro.frontend.faroservice.auth.facebook.FacebookSignupJob;
import com.zik.faro.frontend.faroservice.auth.FaroSignupJob;
import com.zik.faro.frontend.faroservice.auth.SignInJobResult;
import com.zik.faro.frontend.faroservice.auth.SignInJobResultHandler;

import java.net.HttpURLConnection;
import java.text.MessageFormat;

/**
 * Created by granganathan on 1/24/16.
 */
public class SignupActivity extends Activity {
    private String TAG = "SignupActivity";

    private EditText nameTextBox;
    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText confirmPasswordBox;
    private EditText serverIPAddressEditText;

    private LinearLayout signupActivityProgressBarLayout;
    private RelativeLayout signupActivityDetailsLayout;

    private Intent appLandingPageIntent;

    private CallbackManager callbackManager;   // Facebook login callbackmanager

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        context = this;

        // Bind the data fields with the corresponding View components
        nameTextBox = (EditText)findViewById(R.id.signupName);
        emailTextBox = (EditText)findViewById(R.id.signupEmail);
        passwordTextBox = (EditText)findViewById(R.id.signupPassword);
        confirmPasswordBox = (EditText)findViewById(R.id.confirmPassword);
        serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        // Get the progress bar and login details layouts
        signupActivityProgressBarLayout = (LinearLayout) findViewById(R.id.signupActivityProgressBarLayout);
        signupActivityDetailsLayout = (RelativeLayout) findViewById(R.id.signupActivityDetailsLayout);

        // Set visibility to gone on the progress bar layout
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
                showSignupProgressLayout();
            }
        });
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.i(TAG, MessageFormat.format("fbLogin successful. loginResult =  {0}", loginResult));

                SignInJobResultHandler signupResultHandler = new SignInJobResultHandler() {
                    @Override
                    public void handleResponse(SignInJobResult jobResult) {
                        // Go to App landing page or show popup with appropriate message if response is not successful

                        String email = jobResult.getEmail();
                        try {
                            jobResult.getToken();
                            Log.i(TAG, MessageFormat.format("Successfully signed in user {0}", email));

                            // Signup through facebook successful. Go to App landing page
                            startActivity(appLandingPageIntent);
                            finish();
                        } catch (FirebaseAuthException e) {
                            Log.e(TAG, MessageFormat.format("Firebase auth for user {0} failed", email), e);

                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Log.i(TAG, MessageFormat.format("User {0} already exists", email));
                            }
                        } catch (FaroHttpException e) {
                            if (e.getHttpError().getCode() == HttpURLConnection.HTTP_CONFLICT) {
                                Log.i(TAG, MessageFormat.format("User {0} already exists", email));

                                // Prompt to sign in using username back on the LoginActivity Page
                                Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                                loginIntent.putExtra("email", email);
                                startActivity(loginIntent);
                                finish();
                            }
                        } catch (SignInJobException e) {
                            Log.e(TAG, "failed to send signup request", e);
                            LoginManager.getInstance().logOut();
                            showSignupFailurePopup("Signup failed. Check your connection");
                        }

                        // Make the progress bar go away and make the signup details page visible
                        showSignupDetailsLayout();
                    }
                };

                FaroExecutionManager.execute(new FacebookSignupJob(loginResult.getAccessToken())
                        .addResultHandler((Activity) context, signupResultHandler));
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "fbLogin cancelled");
                updateLayoutOnUiThread();
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "fbLogin error", error);
                updateLayoutOnUiThread();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showSignupDetailsLayout() {
        signupActivityProgressBarLayout.setVisibility(View.GONE);
        signupActivityDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void showSignupProgressLayout() {
        signupActivityDetailsLayout.setVisibility(View.GONE);
        signupActivityProgressBarLayout.setVisibility(View.VISIBLE);
    }

    private void updateLayoutOnUiThread() {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showSignupDetailsLayout();
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
        Log.i(TAG, "signup button clicked");

        // Get the activity_signup data and validate
        String name = nameTextBox.getText().toString();
        String email = emailTextBox.getText().toString().toLowerCase();
        String password = passwordTextBox.getText().toString();
        String confirmPassword = confirmPasswordBox.getText().toString();

        String serverIpAddress = serverIPAddressEditText.getText().toString().trim();
        if (!Strings.isNullOrEmpty(serverIpAddress)) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIpAddress);
        }
        Log.i(TAG, MessageFormat.format("Overriding serverIP to {0}", ((FaroApplication)getApplication()).getAppServerIp()));

        if (validate(name, email, password, confirmPassword)) {
            final FaroUser newFaroUser = new FaroUser(email, name, null, null, null, null, null);

            SignInJobResultHandler signupResultHandler = new SignInJobResultHandler() {
                @Override
                public void handleResponse(SignInJobResult jobResult) {
                    Log.i(TAG, MessageFormat.format("signInJobResult = {0}", jobResult));

                    // Go to App landing page or show popup with appropriate message if response is not successful
                    try {
                        jobResult.getToken();
                        // Signup successful. Go to App landing page
                        startActivity(appLandingPageIntent);
                        finish();
                    } catch (FaroHttpException e) {
                        if (e.getHttpError().getCode() == HttpURLConnection.HTTP_CONFLICT) {
                            showSignupFailurePopup(MessageFormat.format("User {0} already exists", newFaroUser.getId()));
                        } else {
                            // default message for all other error codes
                            showSignupFailurePopup("Cannot complete signup at this time. Try again");
                        }
                    } catch (SignInJobException e) {
                        Log.e(TAG, "signup process failed", e);
                        showSignupFailurePopup("Signup failed. Check your connection");
                    } catch (FirebaseAuthException e) {
                        // Should never get this exception
                        Log.e(TAG, MessageFormat.format("Unexpected firebase failure encountered during native signup of user {0}", newFaroUser.getId()), e);
                    }
                }
            };

            FaroExecutionManager.execute(new FaroSignupJob(newFaroUser, password).addResultHandler(this, signupResultHandler));
        }
    }

    private void showSignupFailurePopup(String message) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.sign_in_failure_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView textView = (TextView)container.findViewById(R.id.signInFailureTextView);
        textView.setText(message);
        Button okButton = (Button)container.findViewById(R.id.signinFailureOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(signupActivityDetailsLayout, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }
}
