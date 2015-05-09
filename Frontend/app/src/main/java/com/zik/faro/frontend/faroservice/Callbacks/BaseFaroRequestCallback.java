package com.zik.faro.frontend.faroservice.Callbacks;


import com.squareup.okhttp.Request;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;

public interface BaseFaroRequestCallback<T>{
    public void onFailure(Request request, IOException ex);
    public void onResponse(T t, HttpError error);
}
