package com.zik.faro.frontend.faroservice.auth;

import com.zik.faro.frontend.faroservice.HttpError;

import java.text.MessageFormat;

/**
 * Created by gaurav on 7/3/17.
 */

public class FaroHttpException extends Exception {
    private HttpError httpError;

    public FaroHttpException(HttpError httpError) {
        super(MessageFormat.format("Faro app server returned http status {0} ", httpError.getCode()));
        this.httpError = httpError;
    }

    public HttpError getHttpError() {
        return httpError;
    }
}
