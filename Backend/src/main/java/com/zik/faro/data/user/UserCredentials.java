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
    private String userCredsUUid;

    private UserCredentials() {}

    public UserCredentials(String email, String encryptedPassword, String userCredsUUid) {
        this.email = email;

        // Store the encrypted encryptedPassword
        this.encryptedPassword = encryptedPassword;

        this.userCredsUUid = userCredsUUid;
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

    public String getUserCredsUUid() {
        return userCredsUUid;
    }

    public void setUserCredsUUid(String userCredsUUid) {
        this.userCredsUUid = userCredsUUid;
    }
}
