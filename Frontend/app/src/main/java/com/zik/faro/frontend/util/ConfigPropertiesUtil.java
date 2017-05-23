package com.zik.faro.frontend.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by granganathan on 1/7/17.
 */

public class ConfigPropertiesUtil {
    private static final String TAG = "ConfigPropertiesUtil";
    private static final String PROPS_FILE_NAME = "faro-config.properties";

    public static String getProperty(String key, Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(PROPS_FILE_NAME);
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e) {
            Log.e(TAG, "unable to get the property " + key);
            return null;
        }
    }
}
