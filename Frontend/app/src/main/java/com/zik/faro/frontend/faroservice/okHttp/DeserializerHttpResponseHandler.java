package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.request.CustomCalendarDeserializer;
import com.zik.faro.frontend.faroservice.request.CustomCalendarSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;

public class DeserializerHttpResponseHandler<T> implements Callback {
    private BaseFaroRequestCallback callback;
    private Class<T> responseClass;
    private static final Gson mapper = gsonBuilder();
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

    private static Gson gsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(Calendar.class, new CustomCalendarSerializer());
        builder.registerTypeHierarchyAdapter(Calendar.class, new CustomCalendarDeserializer());
        return builder.create();
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
        if (responseClass == String.class) {
            callback.onResponse(jsonResponse, null);
        } else if(responseClass != null){
            callback.onResponse(mapper.fromJson(jsonResponse, responseClass), null);
        } else if(type != null){
            //use GSON to deserialize the response to a generic list Type
            // /*Test string */ jsonResponse = "[{\"controlFlag\":\"false\",\"eventId\":\"17bb0753-90cb-4de1-953e-7222870af2de\",\"eventName\":\"MySampleEvent1\",\"status\":\"OPEN\"}, {\"controlFlag\":\"false\",\"eventId\":\"27bb0753-90cb-4de1-953e-7222870af2de\",\"eventName\":\"MySampleEvent2\",\"status\":\"OPEN\"}]";
            callback.onResponse(mapper.fromJson(jsonResponse, type), null);
        }
    }
}
