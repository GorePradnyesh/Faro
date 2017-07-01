package com.zik.faro.frontend.faroservice;

import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

/**
 * Created by gaurav on 6/25/17.
 */

public interface SignupJobResultHandler<T> {
    void handleResponse(OkHttpResponse<T> response);
}
