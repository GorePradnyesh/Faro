package com.zik.faro.frontend;


import com.squareup.okhttp.Request;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Utils {


    static class BaseTestCallbackHandler{
        public final Semaphore waitSem;
        public boolean failed;
        public final int expectedCode;

        public boolean unexpectedResponseCode;
        public String errorMessage;

        BaseTestCallbackHandler(Semaphore semaphore, int expectedCode){
            this.waitSem = semaphore;
            this.failed = false;
            this.expectedCode = expectedCode;
            this.unexpectedResponseCode = false;
        }
    }

    final static class TestSignupCallback
            extends Utils.BaseTestCallbackHandler
            implements BaseFaroRequestCallback<String> {
        String token;
        TestSignupCallback(Semaphore semaphore, int expectedCode){
            super(semaphore, expectedCode);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            waitSem.release();
        }

        @Override
        public void onResponse(String token, HttpError error){
            if(error != null){
                this.failed = true;
                return;
            }
            this.token = token;
            waitSem.release();
        }
    }

}
