package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.zik.faro.frontend.faroservice.request.CustomCalendarDeserializer;
import com.zik.faro.frontend.faroservice.request.CustomCalendarSerializer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class BaseFaroOKHttpWrapper {
    private static OkHttpClient client_s;
    protected final String DEFAULT_CONTENT_TYPE = "application/json";
    protected final static Gson mapper  = gsonBuilder();
    protected final OkHttpClient httpClient;
    protected URL baseUrl;
    protected URL baseHandlerURL;
    protected final String authHeaderName = "Authentication";

    private static Gson gsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(Calendar.class, new CustomCalendarSerializer());
        builder.registerTypeHierarchyAdapter(Calendar.class, new CustomCalendarDeserializer());
        return builder.create();
    }

    BaseFaroOKHttpWrapper(final URL baseUrl, final String pathPrefix) {
        this.baseUrl = baseUrl;
        this.baseHandlerURL = this.constructUrl(this.baseUrl, pathPrefix);
        httpClient = getOkHttpClient();
        //httpClient.setConnectTimeout(600, TimeUnit.SECONDS);
        //httpClient.setReadTimeout(600, TimeUnit.SECONDS);
    }
    
    public URL constructUrl(final URL baseUrl, final String pathPrefix) {
        String prefix = "";
        URL outUrl = baseUrl;
        // Adjust slashes at the beginning of prefix
        if(pathPrefix.startsWith("/")){
            prefix = pathPrefix.substring(1, pathPrefix.length() -1);
        }else{
            prefix = pathPrefix;
        }
        // Adjust slashes at the end of prefix
        if(!prefix.endsWith("/")){
            prefix = prefix + "/";
        }
        try {
            if(!outUrl.toString().endsWith("/")){
                outUrl = new URL(outUrl.toString() + "/");
            }
            return new URL(outUrl.toString() + prefix);
        } catch (MalformedURLException e) {
            throw new RuntimeException("unexpected exception while normalizing url:" + this.baseUrl);
        }
    }

    private static OkHttpClient getOkHttpClient(){
        if(client_s != null){
            return client_s;
        }
        synchronized (BaseFaroOKHttpWrapper.class){
            if(client_s == null){
                client_s = new OkHttpClient();
            }
            return client_s;
        }
    }
}
