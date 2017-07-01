package com.zik.faro.frontend.faroservice.Callbacks;


import com.squareup.okhttp.Request;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;

public interface BaseFaroRequestCallback<T> {
    void onFailure(Request request, IOException ex);
    void onResponse(T t, HttpError error);
}
