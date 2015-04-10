package com.zik.faro.commons;

/**
 * Created by granganathan on 4/8/15.
 */
public class FaroResponseEntity {
    private int faroStatusCode;
    private String faroStatusMessage;

    public FaroResponseEntity(int faroStatusCode, String faroStatusMessage) {
        this.faroStatusCode = faroStatusCode;
        this.faroStatusMessage = faroStatusMessage;
    }

    public int getFaroStatusCode() {
        return faroStatusCode;
    }

    public String getFaroStatusMessage() {
        return faroStatusMessage;
    }
}
