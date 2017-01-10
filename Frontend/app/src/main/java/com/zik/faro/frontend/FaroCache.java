package com.zik.faro.frontend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class FaroCache {
    private String TAG = "FaroCache";
    private static FaroCache instance = null;
    private static SharedPreferences sharedPrefs;
    private static final String SHARED_PREFS_NAME = "FaroCachetFile";

    private FaroCache(Context context){
        sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FaroCache getOrCreateFaroUserContextCache(Context context){
        if (instance == null) {
            instance = new FaroCache(context);
        }
        return instance;
    }

    public static synchronized FaroCache getFaroUserContextCache(){
        if (instance == null) {
            throw new IllegalStateException("FaroUserContext cache has not been initialized.");
        }
        return instance;
    }

    public void saveFaroCacheToDisk(String key, String value){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.commit();

        String storedValue = sharedPrefs.getString(key, "");
        Log.i(TAG, key + "=" + storedValue);
    }

    public String loadFaroCacheFromDisk(String key){
        return sharedPrefs.getString(key, "");
    }

    private static void deleteFaroCacheFromDisk() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }

}
