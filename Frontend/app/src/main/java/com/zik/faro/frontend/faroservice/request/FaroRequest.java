package com.zik.faro.frontend.faroservice.request;

import java.util.HashMap;
import java.util.Map;

public class FaroRequest {
    public final String humanReadableId;
    public final String host;
    public final int port;
    public final boolean isSecure;
    public final String path;
    public Map<String, String> headers;

    private final String protocol;

    public FaroRequest(String humanReadableId, String host, int port, boolean isSecure, String path) {
        this.humanReadableId = humanReadableId;
        this.host = host;
        this.port = port;
        this.isSecure = isSecure;
        this.protocol = isSecure? "https" : "http";
        if(!path.startsWith("/")){
            this.path = "/" + path;
        }else{
            this.path = path;
        }
        this.headers = new HashMap<>();
    }

    @Override
    public String toString() {
        return protocol + "://" + host + ":" + port + path;
    }

    public String getUrl(){
        return this.toString();
    }


}
