package com.zik.faro.frontend;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by granganathan on 6/26/16.
 */
public class FaroApplication extends Application {
    private String TAG = "FaroApplication";

    // Updated your class body:
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Log.i(TAG, "Initialized facebook SDK");

        /*String accessTokenStr = "EAACEdEose0cBALLX4c6jvsnNTZBseKefRAadgzDmZBIYxGsan3EpzNGbbx2fIexEc2" +
                "Fo8tJhD9OQOithvIacBDGZCjiFXHqlywn7RIEd8CvdoEyZBny0CWrrtPXT3F4nFqQC7" +
                "EYK646v9DO5GivnnjPGWZBXfVkD5cPrhALgeEgZDZD";
        String applicationId = "145634995501895";
        String userId = "10155071787680006";

        accessToken = new AccessToken(accessTokenStr, applicationId, userId, null,
                null, AccessTokenSource.FACEBOOK_APPLICATION_WEB, new Date(1467673200), null);

        String imageUrl = "https://scontent.xx.fbcdn.net/v/t1.0-9/" +
                "11129914_10155366688470006_9052762267197352743_n.jpg?" +
                "oh=33523da8c64ed37a08b1ccc528a400d2&oe=5805A228";
        Log.i(TAG, "Downloading photo ....");

        String albumName = "test album010";*/

        // Obtain user profile info
        //obtainUserData();

        // Create Album
        //createAlbum(albumName);

        // Upload photos to Album from camera roll
        //uploadPhotos();

        // Download photos from Album using public link to each photo
        // and save to camera roll
        //downloadPhoto(imageUrl);

    }

}

