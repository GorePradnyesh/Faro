package com.zik.faro.commons;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by granganathan on 4/8/15.
 */

@XmlRootElement
public class FaroResponseEntity {
    private int faroStatusCode;
    private String faroStatusMessage;

    public FaroResponseEntity(int faroStatusCode, String faroStatusMessage) {
        this.faroStatusCode = faroStatusCode;
        this.faroStatusMessage = faroStatusMessage;
    }

    private FaroResponseEntity() {}

    public int getFaroStatusCode() {
        return faroStatusCode;
    }

    public String getFaroStatusMessage() {
        return faroStatusMessage;
    }
}
