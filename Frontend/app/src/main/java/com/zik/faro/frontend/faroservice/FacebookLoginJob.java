package com.zik.faro.frontend.faroservice;


import com.facebook.AccessToken;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.zik.faro.frontend.EventListHandler.serviceHandler;

/**
 * Created by gaurav on 6/29/17.
 */

public class FacebookLoginJob extends FaroBaseSignInJob {
    private String TAG = "FacebookLoginJob";
    private AccessToken facebookAccessToken;
    private static final Long FIREBASE_SIGNIN_TIMEOUT_SECS = 30L;

    public FacebookLoginJob(AccessToken facebookAccessToken) {
        super(facebookAccessToken.getUserId());
        this.facebookAccessToken = facebookAccessToken;
    }

    @Override
    public OkHttpResponse<String> signIn() throws IOException {
        return handleFacebookAccessToken();
    }

    private OkHttpResponse<String> handleFacebookAccessToken() {
        // Get Firebase AuthCredential object from the Fb access token
        AuthCredential authCredential = FacebookAuthProvider.getCredential(facebookAccessToken.getToken());

        // Now proceed with the login workflow
        // Sign in the user using the Firebase credential.
        Task<AuthResult> signInWithCredentialTask = FirebaseAuth.getInstance().signInWithCredential(authCredential);

        try {
            AuthResult authResult = Tasks.await(signInWithCredentialTask, FIREBASE_SIGNIN_TIMEOUT_SECS, TimeUnit.SECONDS);
            return loginWithFirebaseToken(authResult.getUser());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return new OkHttpResponse<String>(null, null);
    }

    protected OkHttpResponse<String> faroSignIn(FirebaseUser firebaseUser, String firebaseAuthToken) throws IOException {
        return serviceHandler.getFirebaseLoginHandler().login(firebaseUser.getEmail(), null, firebaseAuthToken, true);
    }

    private OkHttpResponse<String> loginWithFirebaseToken(FirebaseUser firebaseUser) {
        Task<GetTokenResult> getTokenResultTask = firebaseUser.getToken(true);

        try {
            GetTokenResult getTokenResult = Tasks.await(getTokenResultTask);

            // Now login to Faro server using the firebase token
            return faroSignIn(firebaseUser, getTokenResult.getToken());

            /*if (response.isSuccessful() && response.getResponseObject() != null) {
                // Login successful. Go to App landing page
                startActivity(appLandingPageIntent);
                finish();
            } else if (response.getHttpError() != null) {
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
            }*/


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new OkHttpResponse<String>(null, null);
        //updateLayoutOnUiThread();
    }

}
