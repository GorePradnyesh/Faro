package com.zik.faro.frontend.faroservice.okHttp;

import com.google.common.base.MoreObjects;
import com.zik.faro.frontend.faroservice.HttpError;

/**
 * Created by gaurav on 6/24/17.
 */

public class OkHttpResponse<T> {
    private T responseObject;
    private HttpError httpError;

    public OkHttpResponse(T responseObject, HttpError httpError) {
        this.responseObject = responseObject;
        this.httpError = httpError;
    }

    public OkHttpResponse(HttpError httpError) {
        this(null, httpError);
    }

    public T getResponseObject() {
        return responseObject;
    }

    public HttpError getHttpError() {
        return httpError;
    }

    public boolean isSuccessful() {
        if (httpError == null) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("responseObject", responseObject)
                .add("httpError", httpError)
                .toString();
    }
}
