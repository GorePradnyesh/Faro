package com.zik.faro.data.user;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by granganathan on 2/11/15.
 */
@Entity
@XmlRootElement
public class UserCredentials {
    @Id
    @Index
    private String             email;
    private String             password;

    public UserCredentials(String email, String password) {
        this.email = email;

        // Store the encrypted password
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
