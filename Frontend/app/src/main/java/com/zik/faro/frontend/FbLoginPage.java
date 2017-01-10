package com.zik.faro.frontend;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.common.collect.Lists;

import java.text.MessageFormat;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by granganathan on 9/18/16.
 */
public class FbLoginPage extends Fragment {
    private CallbackManager callbackManager;
    private static final String TAG = "FbLoginPage";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_fb_login_page, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.i(TAG, MessageFormat.format("accesstoken         = {0}", loginResult.getAccessToken().getToken()));
                Log.i(TAG, MessageFormat.format("current accesstoken = {0} ", AccessToken.getCurrentAccessToken().getToken()));
                Log.i(TAG, MessageFormat.format("recently granted permissions = {0}", loginResult.getRecentlyGrantedPermissions()));
                Log.i(TAG, MessageFormat.format("recently denied  permissions = {0}", loginResult.getRecentlyDeniedPermissions()));
            }

            @Override
            public void onCancel() {
                // App code
                Log.i(TAG, "cancelled login ");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(TAG, "error logging into facebook", exception);
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken == null || accessToken.isExpired()) {
            LoginManager.getInstance().logInWithReadPermissions(this, Lists.newArrayList("user_photos"));
        } else {
            printAccessToken(accessToken);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void printAccessToken(AccessToken accessToken) {
        Log.i(TAG, MessageFormat.format("accessToken : = {0}  {1}", accessToken.getToken(), accessToken.getPermissions()));
    }
}
