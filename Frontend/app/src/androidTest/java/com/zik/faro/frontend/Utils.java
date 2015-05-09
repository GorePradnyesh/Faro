package com.zik.faro.frontend;


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


}
