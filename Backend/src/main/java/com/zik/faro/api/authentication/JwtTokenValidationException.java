package com.zik.faro.api.authentication;

/**
 * Created by granganathan on 3/8/15.
 */
public class JwtTokenValidationException extends Exception {
    public JwtTokenValidationException(String message) {
        super(message);
    }
}
