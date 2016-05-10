package com.zik.faro.frontend.faroservice.auth;

import android.util.Log;

import com.zik.faro.frontend.faroservice.FaroServiceHandler;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nakulshah on 5/7/16.
 */
public class FaroUserContext {
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
}

