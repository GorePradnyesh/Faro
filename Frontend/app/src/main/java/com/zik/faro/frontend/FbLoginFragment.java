package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.text.MessageFormat;

/**
 * Created by granganathan on 9/18/16.
 */
public class FbLoginFragment extends Fragment {
    private CallbackManager callbackManager;
    private static final String TAG = "FbLoginFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_fb_login_page, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
