package com.zik.faro.frontend;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.util.ConfigPropertiesUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;


/**
 * Created by granganathan on 6/26/16.
 */
public class FaroApplication extends Application {
    private String TAG = "FaroApplication";
    private static final String APP_SERVER_IP_KEY = "APP_SERVER_IP";
    private static final String MAX_IMAGES_UPLOAD_KEY = "MAX_IMAGES_UPLOAD";
    private static final String BASE_URL_TEMPLATE = "http://{0}:8080/v1/";
    private String appServerIP;
    private int maxImagesUpload;
    private String appServerBaseUrl;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Log.i(TAG, "Initialized facebook SDK");

        // Read app config properties
        appServerIP = ConfigPropertiesUtil.getProperty(APP_SERVER_IP_KEY, this);
        appServerBaseUrl = MessageFormat.format(BASE_URL_TEMPLATE, appServerIP);
        setupFaroServiceHandler();

        //Creating the global Handlers and initializing the Adapters for all
        createGlobalHandlersAndAdapters();

        maxImagesUpload = Integer.parseInt(ConfigPropertiesUtil.getProperty(MAX_IMAGES_UPLOAD_KEY, this));
    }

    private void createGlobalHandlersAndAdapters() {
        EventListHandler eventListHandler = EventListHandler.getInstance();
        UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();

        if (eventListHandler.acceptedEventAdapter == null) {
            eventListHandler.acceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }

        if (eventListHandler.notAcceptedEventAdapter == null) {
            eventListHandler.notAcceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }

        if (userFriendListHandler.userFriendAdapter == null){
            userFriendListHandler.userFriendAdapter = new UserFriendAdapter(this, R.layout.friend_row_style);
        }
    }

    public String getAppServerIp() {
        return appServerIP;
    }

    public String getAppServerBaseUrl() {
        return appServerBaseUrl;
    }

    public void overRideAppServerIp(String appServerIP) {
        this.appServerIP = appServerIP;
        this.appServerBaseUrl = MessageFormat.format(BASE_URL_TEMPLATE, appServerIP);
        setupFaroServiceHandler();
    }

    private void setupFaroServiceHandler() {
        try {
            FaroServiceHandler.initializeInstance(new URL(appServerBaseUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed URL, could not initialize FaroServiceHandler");
        }
    }

    public int getMaxImagesUploaded() {
        return maxImagesUpload;
    }
}

