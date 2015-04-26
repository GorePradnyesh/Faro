package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.net.URL;

public class BaseFaroOKHttpWrapper {
    private static OkHttpClient client_s;

    protected final static String DEFAULT_CONTENT_TYPE = "application/json";
    protected final static Gson mapper = new Gson();

    protected final OkHttpClient httpClient;
    protected URL baseUrl;
    protected URL baseHandlerURL;

    BaseFaroOKHttpWrapper(final URL baseUrl, final String pathPrefix){
        this.baseUrl = baseUrl;
        String prefix = "";
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
            if(!this.baseUrl.toString().endsWith("/")){
                this.baseUrl = new URL(this.baseUrl.toString() + "/");
            }
            this.baseHandlerURL = new URL(this.baseUrl.toString() + prefix);
        } catch (MalformedURLException e) {
            throw new RuntimeException("unexpected exception while normalizing url:" + this.baseUrl);
        }
        this.httpClient = getOkHttpClient();
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
