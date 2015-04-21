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
    private String encryptedPassword;

    private UserCredentials() {}

    public UserCredentials(String email, String encryptedPassword) {
        this.email = email;

        // Store the encrypted encryptedPassword
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
