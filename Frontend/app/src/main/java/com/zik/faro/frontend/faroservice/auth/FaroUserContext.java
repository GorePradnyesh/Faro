package com.zik.faro.frontend.faroservice.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.zik.faro.frontend.EventListHandler;
import com.zik.faro.frontend.FaroCache;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;

import java.net.MalformedURLException;
import java.net.URL;


public class FaroUserContext{
    private String email;
    private static FaroUserContext faroUserContext = null;

    public static FaroUserContext getInstance(){
        if (faroUserContext != null){
            return faroUserContext;
        }
        synchronized (FaroUserContext.class)
        {
            if(faroUserContext == null) {
                faroUserContext = new FaroUserContext();
            }
            return faroUserContext;
        }
    }

    private FaroUserContext(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FaroUserContext(String email){
        this.email = email;
        //FaroCache faroCache = FaroCache.getOrCreateFaroUserContextCache();
    }
}

