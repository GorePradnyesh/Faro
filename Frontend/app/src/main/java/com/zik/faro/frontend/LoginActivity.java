package com.zik.faro.frontend;

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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.zik.faro.frontend.faroservice.auth.FaroHttpException;
import com.zik.faro.frontend.faroservice.auth.SignInJobException;
import com.zik.faro.frontend.faroservice.auth.facebook.FacebookLoginJob;
import com.zik.faro.frontend.faroservice.auth.FaroLoginJob;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.SignInJobResult;
import com.zik.faro.frontend.faroservice.auth.SignInJobResultHandler;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.net.HttpURLConnection;
import java.text.MessageFormat;

public class LoginActivity extends Activity {
    private String TAG = "LoginActivity";

    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText serverIPAddressEditText;
    private LoginButton fbLoginButton;

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private FaroCache faroCache;

    private Intent appLandingPageIntent;

    private LinearLayout loginActivityProgressBarLayout;
    private RelativeLayout loginActivityDetailsLayout;

    private CallbackManager callbackManager;   // Facebook login callbackmanager
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        // Create the facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        // Bind the data fields with the corresponding View components
        emailTextBox = (EditText)findViewById(R.id.TFEmail);
        passwordTextBox = (EditText)findViewById(R.id.TFPassword);
        serverIPAddressEditText = (EditText)findViewById(R.id.ipAddress);

        // Get the progress bar and login details layouts
        loginActivityProgressBarLayout = (LinearLayout) findViewById(R.id.loginActivityProgressBarLayout);
        loginActivityDetailsLayout = (RelativeLayout) findViewById(R.id.loginActivityDetailsLayout);

        // Set visibility to gone on the progress bar layout
        loginActivityProgressBarLayout.setVisibility(View.GONE);

        // Intent for transitioning to AppLanding page after a successful login
        appLandingPageIntent = new Intent(LoginActivity.this, AppLandingPage.class);

        // Setup token cache
        TokenCache tokenCache = TokenCache.getOrCreateTokenCache(LoginActivity.this);

        // Setup Faro Cache
        faroCache = FaroCache.getOrCreateFaroUserContextCache(LoginActivity.this);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        // Setup the facebook signup/connect with button
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions("email", "public_profile", "user_photos", "user_friends");
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "fb login button pressed. Bring up the progress bar");
                if (AccessToken.getCurrentAccessToken() == null) {
                    showLoginProgressLayout();
                }
            }
        });
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                loginUsingFacebook();
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

        // Check if we have arrived on LoginActivity from the SignupActivity because third party signup process
        // determined that the user's account already exists
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !Strings.isNullOrEmpty(bundle.getString("email"))) {
            emailTextBox.setText(bundle.getString("email"));
            emailTextBox.setError("You already have an account with this email. Login by entering the password");
            return;
        }

        try {
            String token = tokenCache.getToken();

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
        if (AccessToken.getCurrentAccessToken() != null) {
            showLoginProgressLayout();
            loginUsingFacebook();
        }
    }

    private void loginUsingFacebook() {
        Log.i(TAG, MessageFormat.format("fbLogin successful. accessToken =  {0}", AccessToken.getCurrentAccessToken()));

        SignInJobResultHandler signupResultHandler = new SignInJobResultHandler() {
            @Override
            public void handleResponse(SignInJobResult jobResult) {
                // Go to App landing page or show popup with appropriate message if response is not successful

                String email = jobResult.getEmail();
                try {
                    jobResult.getToken();
                    // Login through facebook successful. Go to App landing page
                    startActivity(appLandingPageIntent);
                    finish();
                } catch (FirebaseAuthException e) {
                    Log.e(TAG, MessageFormat.format("Firebase auth for user {0} failed", email), e);

                    if (e instanceof FirebaseAuthUserCollisionException) {
                        Log.i(TAG, MessageFormat.format("Username {0} already exists", email));
                    }
                } catch (FaroHttpException e) {
                    HttpError loginHttpError = e.getHttpError();
                    if (loginHttpError.getCode() == HttpURLConnection.HTTP_CONFLICT) {
                        // TODO : Since the Facebook authentication completed successfully, we can make use of
                        // the user's facebook token for image upload.
                        // Also link the users's facebook account with the Faro account and allow future login with facebook as well

                        // Set the email text box with the facebook user id
                        emailTextBox.setText(email);

                        // Pop up failure message asking user to enter password
                        showLoginFailurePopup(MessageFormat.format("Username {0} already exists. Enter password to continue", email));
                    } else if (loginHttpError.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // Pop up failure message asking user to signup first
                        showLoginFailurePopup(MessageFormat.format("Username {0} does not exist. Signup to create an account", email));
                    } else {
                        // default message for all other error codes
                        showLoginFailurePopup("Cannot complete login at this time. Try again");
                    }
                } catch (SignInJobException e) {
                    Log.e(TAG, "failed to send login request", e);
                    LoginManager.getInstance().logOut();
                    showLoginFailurePopup("Login failed. Check your connection");
                }

                // Make the progress bar go away and make the login details page visible
                showLoginDetailsLayout();
            }
        };

        // Execute login through facebook workflow
        FaroExecutionManager.execute(new FacebookLoginJob(AccessToken.getCurrentAccessToken())
                .addResultHandler((Activity) context, signupResultHandler));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoginDetailsLayout() {
        loginActivityProgressBarLayout.setVisibility(View.GONE);
        loginActivityDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void showLoginProgressLayout() {
        loginActivityDetailsLayout.setVisibility(View.GONE);
        loginActivityProgressBarLayout.setVisibility(View.VISIBLE);
    }

    private void updateLayoutOnUiThread() {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showLoginDetailsLayout();
            }
        });
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

        // Get login creds
        final String email = emailTextBox.getText().toString();
        final String password = passwordTextBox.getText().toString();

        // server IP
        String serverIpAddress = serverIPAddressEditText.getText().toString().trim();
        if (!Strings.isNullOrEmpty(serverIpAddress)) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIpAddress);
        }
        Log.i(TAG, MessageFormat.format("Overriding serverIP to {0}", ((FaroApplication)getApplication()).getAppServerIp()));

        if (validate(email, password)) {
            SignInJobResultHandler signupResultHandler = new SignInJobResultHandler() {

                @Override
                public void handleResponse(SignInJobResult jobResult) {
                    Log.i(TAG, MessageFormat.format("signInJobResult = {0}", jobResult));

                    // Go to App landing page or show popup with appropriate message if response is not successful
                    try {
                        jobResult.getToken();
                        // Login successful. Go to App landing page
                        startActivity(appLandingPageIntent);
                        finish();
                    } catch (FaroHttpException e) {
                        HttpError httpError = e.getHttpError();

                        Log.i(TAG, MessageFormat.format("login failed with error {0}", httpError));

                        if (httpError.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            // Invalid login
                            showLoginFailurePopup("Incorrect username or password. Please try again");
                        } else if (httpError.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            // User does not exist
                            showLoginFailurePopup(MessageFormat.format("Account with username {0} does not exist. Sign up to create an account", email));
                        } else {
                            // default message for all other error codes
                            showLoginFailurePopup("Cannot complete login at this time. Try again");
                        }
                    } catch (SignInJobException e) {
                        Log.e(TAG, "login process failed", e);
                        showLoginFailurePopup("Login failed. Check your connection");
                    } catch (FirebaseAuthException e) {
                        // Should never get this exception
                        Log.e(TAG, MessageFormat.format("Unexpected firebase failure encountered during native login of user {0}", email), e);
                    }
                }
            };

            FaroExecutionManager.execute(new FaroLoginJob(email, password)
                    .addResultHandler(this, signupResultHandler));
        }

    }

    private void showLoginFailurePopup(String message) {
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
        popupWindow.showAtLocation(loginActivityDetailsLayout, Gravity.CENTER, 0, 0);
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
}
