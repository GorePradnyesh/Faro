package com.zik.faro.api.responder;

import com.zik.faro.data.user.FaroUser;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by granganathan on 2/27/15.
 */
@XmlRootElement
public class FaroSignupDetails {
    private FaroUser faroUser;
    private String password;

    public FaroSignupDetails(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
    }

    private FaroSignupDetails() {}

    public FaroUser getFaroUser() {
        return faroUser;
    }

    public String getPassword() {
        return password;
    }

    // TODO : Will have to make the setters private and still have jaxb working fine
    public void setFaroUser(FaroUser faroUser) {
        this.faroUser = faroUser;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
