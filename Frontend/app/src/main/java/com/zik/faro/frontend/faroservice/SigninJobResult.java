package com.zik.faro.frontend.faroservice;

import com.google.firebase.auth.FirebaseAuthException;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpResponse;

/**
 * Created by gaurav on 7/1/17.
 */

public class SigninJobResult {
    private OkHttpResponse<String> okHttpResponse;
    private FirebaseAuthException firebaseAuthException;
    private Exception jobException;

    public SigninJobResult() {

    }

    public OkHttpResponse<String> getOkHttpResponse() {
        return okHttpResponse;
    }

    public void setOkHttpResponse(OkHttpResponse<String> okHttpResponse) {
        this.okHttpResponse = okHttpResponse;
    }

    public FirebaseAuthException getFirebaseAuthException() {
        return firebaseAuthException;
    }

    public void setFirebaseAuthException(FirebaseAuthException firebaseAuthException) {
        this.firebaseAuthException = firebaseAuthException;
    }

    public Exception getJobException() {
        return jobException;
    }

    public void setJobException(Exception jobException) {
        this.jobException = jobException;
    }
}
