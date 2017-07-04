package com.zik.faro.frontend.faroservice.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.zik.faro.frontend.faroservice.HttpError;

/**
 * Created by gaurav on 7/1/17.
 */

public class SignInJobResult {
    private String email;
    private String token;
    private HttpError httpError;
    private FirebaseAuthException firebaseAuthException;
    private Exception jobException;

    public SignInJobResult(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public SignInJobResult(String email, HttpError httpError) {
        this.email = email;
        this.httpError = httpError;
    }

    public SignInJobResult(String email, Exception jobException) {
        this.email = email;
        this.jobException = jobException;
    }

    public SignInJobResult(String email, FirebaseAuthException firebaseAuthException) {
        this.email = email;
        this.firebaseAuthException = firebaseAuthException;
    }

    public String getToken() throws FirebaseAuthException, FaroHttpException, SignInJobException {
        if (firebaseAuthException != null) {
            throw firebaseAuthException;
        } else if (httpError != null) {
            throw new FaroHttpException(httpError);
        } else if (jobException != null) {
            throw new SignInJobException(jobException);
        }

        return token;
    }

    public String getEmail() {
        return email;
    }
}
