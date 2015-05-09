package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.io.IOException;

public class DeserializerHttpResponseHandler<T> implements Callback {
    private BaseFaroRequestCallback callback;
    private Class<T> responseClass;
    protected final static Gson mapper = new Gson();

    DeserializerHttpResponseHandler(final BaseFaroRequestCallback<T> callback, final Class<T> clazz) {
        this.callback = callback;
        this.responseClass = clazz;
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
        } else {
            T t = mapper.fromJson(jsonResponse, responseClass);
            callback.onResponse(t, null);
        }
    }
}
