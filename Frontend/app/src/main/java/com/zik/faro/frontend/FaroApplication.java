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
        try {
            FaroServiceHandler.initializeInstance(new URL(appServerBaseUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed URL, could not initialize FaroServiceHandler");
        }

        maxImagesUpload = Integer.parseInt(ConfigPropertiesUtil.getProperty(MAX_IMAGES_UPLOAD_KEY, this));
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
    }

    public int getMaxImagesUploaded() {
        return maxImagesUpload;
    }
}

