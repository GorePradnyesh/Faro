package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.base.Strings;
import com.zik.faro.frontend.faroservice.FacebookLoginJob;
import com.zik.faro.frontend.faroservice.FaroLoginJob;
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
import com.zik.faro.frontend.faroservice.SignupJobResultHandler;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.net.HttpURLConnection;
import java.text.MessageFormat;

public class LoginActivity extends Activity {
    private String TAG = "LoginActivity";

    private FaroServiceHandler serviceHandler;

    private EditText emailTextBox;
    private EditText passwordTextBox;
    private EditText serverIPAddressEditText;
    private LoginButton fbLoginButton;

    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    static FaroCache faroCache;

    private Intent appLandingPageIntent;

    private LinearLayout loginActivityProgressBarLayout;
    private RelativeLayout loginActivityDetailsLayout;

    private CallbackManager callbackManager;   // Facebook login callbackmanager
    private FirebaseAuth firebaseAuth;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        // Create the facebook callback manager
        callbackManager = CallbackManager.Factory.create();

        // Create Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

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
                showLoginProgressLayout();
            }
        });
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "fbLogin successful. loginResult =  " + loginResult);

                SignupJobResultHandler<String> signupResultHandler = new SignupJobResultHandler<String>() {
                    @Override
                    public void handleResponse(OkHttpResponse<String> response) {
                        if (response.isSuccessful() && response.getResponseObject() != null) {
                            // Login successful. Go to App landing page
                            startActivity(appLandingPageIntent);
                            finish();
                        } else if (response.getHttpError() != null) {
                            // Make the progress bar go away and make the login details page visible
                            showLoginDetailsLayout();

                            // Logout of facebook to rest the LoginButton status
                            LoginManager.getInstance().logOut();

                            HttpError loginHttpError = response.getHttpError();
                            if (loginHttpError.getCode() == HttpURLConnection.HTTP_CONFLICT) {
                                // TODO : Since the Facebook authentication completed successfully, we can make use of
                                // the user's facebook token for image upload.
                                // Also link the users's facebook account with the Faro account and allow future login with facebook as well

                                //emailTextBox.setText(firebaseUser.getEmail());

                                //showLoginFailurePopup(MessageFormat.format("Username {0} already exists. Enter password to continue",
                                //        firebaseUser.getEmail()));
                            } else if (loginHttpError.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                //showLoginFailurePopup(MessageFormat.format("Username {0} does not exist. Signup to create an account",
                                //        firebaseUser.getEmail()));
                            }
                        }

                    }
                };

                FaroExecutionManager.execute(new FacebookLoginJob(loginResult.getAccessToken())
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*private void handleFacebookAccessToken(AccessToken token) {
        // Get Firebase AuthCredential object from the Fb access token
        AuthCredential authCredential = FacebookAuthProvider.getCredential(token.getToken());

        // Now proceed with the login workflow
        // Sign in the user using the Firebase credential.
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "firebaseAuth signInWithCredential complete");
                        loginWithFirebaseToken(authResult.getUser());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Log.i(TAG, "user already exists ");
                        }

                        updateLayoutOnUiThread();
                    }
                });
    }*/

    /*private void loginWithFirebaseToken(final FirebaseUser firebaseUser) {
        firebaseUser.getToken(true)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult getTokenResult) {
                        final String firebaseIdToken = getTokenResult.getToken();
                        Log.i(TAG, "firebaseIdToken = " + firebaseIdToken);

                        // Now login to Faro server using the firebase token
                        FaroUser newFaroUser = new FaroUser(firebaseUser.getEmail(), null, null, null, null, null, null);
                        newFaroUser.setFirstName(firebaseUser.getDisplayName());
                        FirebaseUserLoginCallback firebaseUserLoginCallback = new FirebaseUserLoginCallback(firebaseUser);
                        serviceHandler.getFirebaseLoginHandler().login(firebaseUserLoginCallback, newFaroUser.getId(), null, firebaseIdToken);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Could not obtain firebase token", e);
                        updateLayoutOnUiThread();
                    }
                });
    }*/

    /*private class FirebaseUserLoginCallback implements BaseFaroRequestCallback<String> {
        private FirebaseUser firebaseUser;

        public FirebaseUserLoginCallback(FirebaseUser firebaseUser) {
            this.firebaseUser = firebaseUser;
        }

        @Override
        public void onFailure(Request request, IOException ex) {
            Log.i(TAG, "failed to send login request");
            updateLayoutOnUiThread();
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

                // Login successful. Go to App landing page
                startActivity(appLandingPageIntent);
                finish();
            } else {
                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());

                final HttpError loginHttpError = error;
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Make the progress bar go away and make the login details page visible
                        showLoginDetailsLayout();

                        // Logout of facebook to rest the LoginButton status
                        LoginManager.getInstance().logOut();

                        if (loginHttpError.getCode() == HttpURLConnection.HTTP_CONFLICT) {
                            // TODO : Since the Facebook authentication completed successfully, we can make use of
                            // the user's facebook token for image upload.
                            // Also link the users's facebook account with the Faro account and allow future login with facebook as well

                            emailTextBox.setText(firebaseUser.getEmail());

                            showLoginFailurePopup(MessageFormat.format("Username {0} already exists. Enter password to continue",
                                    firebaseUser.getEmail()));
                        } else if (loginHttpError.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            showLoginFailurePopup(MessageFormat.format("Username {0} does not exist. Signup to create an account",
                                    firebaseUser.getEmail()));
                        }
                    }
                });

            }
        }
    }*/

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

        // Get login creds
        final String email = emailTextBox.getText().toString();
        final String password = passwordTextBox.getText().toString();

        Log.i(TAG, MessageFormat.format("username :{0} password :{1}", email, password));

        // server IP
        String serverIpAddress = serverIPAddressEditText.getText().toString().trim();
        if (!Strings.isNullOrEmpty(serverIpAddress)) {
            ((FaroApplication)getApplication()).overRideAppServerIp(serverIpAddress);
        }
        Log.i(TAG, "serverIP = " + ((FaroApplication)getApplication()).getAppServerIp());

        // obtain Faro service handler
        serviceHandler = FaroServiceHandler.getFaroServiceHandler();

        if (validate(email, password)) {
            SignupJobResultHandler<String> signupResultHandler = new SignupJobResultHandler<String>() {

                @Override
                public void handleResponse(OkHttpResponse<String> response) {
                    // Go to event list page or show popup with appropriate message if response is not successful

                    if (response.isSuccessful() && response.getResponseObject() != null) {
                        startActivity(appLandingPageIntent);
                        finish();
                    } else if (!response.isSuccessful()) {
                        HttpError error = response.getHttpError();
                        Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        // Invalid login
                        if (error.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            Log.i(TAG, "username/password not valid");

                            new Handler(context.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    showLoginFailurePopup("Incorrect username or password. Please try again");
                                }
                            });
                        } else if (error.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            new Handler(context.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    showLoginFailurePopup(MessageFormat.format("Account with username {0} does not exist. Sign up to create an account", email));
                                }
                            });
                        }
                    } else {
                        Log.i(TAG, "failed to send login request");

                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showLoginFailurePopup("Login failed. Check your connection");
                            }
                        });
                    }
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

    private void showLoginFailurePopup(String message) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.login_failure_popup, null);
        final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        TextView textView = (TextView)container.findViewById(R.id.loginFailureTextView);
        textView.setText(message);
        Button okButton = (Button)container.findViewById(R.id.loginFailureOkButton);
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
