package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.io.IOException;
import java.lang.reflect.Type;

public class DeserializerHttpResponseHandler<T> implements Callback {
    private BaseFaroRequestCallback callback;
    private Class<T> responseClass;
    protected final static Gson mapper = new Gson();
    private Type type;

    DeserializerHttpResponseHandler(final BaseFaroRequestCallback<T> callback, final Class<T> clazz) {
        this.callback = callback;
        this.responseClass = clazz;
        this.type = null;
    }

    DeserializerHttpResponseHandler(final BaseFaroRequestCallback<T> callback, Type listType) {
        this.callback = callback;
        this.responseClass = null;
        this.type = listType;
    }
    
    @Override
    public void onFailure(Request request, IOException ex) {
        //TODO: Add logging
        ex.printStackTrace();
        callback.onFailure(request, ex);
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String message = "";
            if (response.body() != null) {
                message = response.body().toString();
            }
            callback.onResponse(null, new HttpError(response.code(), message));
            return;
        }

        String jsonResponse = response.body().string();
        if (this.responseClass == String.class) {
            callback.onResponse(jsonResponse, null);
        } else if(responseClass != null){
            T t = mapper.fromJson(jsonResponse, responseClass);
            callback.onResponse(t, null);
        } else if(type != null){
            //use GSON to deserialize the response to a generic list Type
            // /*Test string */ jsonResponse = "[{\"controlFlag\":\"false\",\"eventId\":\"17bb0753-90cb-4de1-953e-7222870af2de\",\"eventName\":\"MySampleEvent1\",\"status\":\"OPEN\"}, {\"controlFlag\":\"false\",\"eventId\":\"27bb0753-90cb-4de1-953e-7222870af2de\",\"eventName\":\"MySampleEvent2\",\"status\":\"OPEN\"}]";
            T collection = mapper.fromJson(jsonResponse, type);
            callback.onResponse(collection, null);
        }
    }
}
