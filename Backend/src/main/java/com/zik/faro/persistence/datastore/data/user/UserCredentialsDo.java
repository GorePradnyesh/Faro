package com.zik.faro.persistence.datastore.data.user;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by granganathan on 2/11/15.
 */
@Entity
@XmlRootElement
public class UserCredentialsDo {
    @Id
    @Index
    private String             email;
    private String encryptedPassword;
    private String userCredsUUid;
    private AuthProvider authProvider;
    @Index
    private String authProviderUserId;

    private UserCredentialsDo() {}

    public UserCredentialsDo(String email, String encryptedPassword, String userCredsUUid) {
        this.authProvider = AuthProvider.FARO;

        this.email = email;
        // Store the encrypted encryptedPassword
        this.encryptedPassword = encryptedPassword;
        this.userCredsUUid = userCredsUUid;
    }

    public UserCredentialsDo(String email, String userCredsUUid, AuthProvider authProvider, String authProviderUserId) {
        this.email = email;
        this.userCredsUUid = userCredsUUid;
        this.authProvider = authProvider;
        this.authProviderUserId = authProviderUserId;
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

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public void setAuthProviderUserId(String authProviderUserId) {
        this.authProviderUserId = authProviderUserId;
    }

    public String getAuthProviderUserId() {
        return authProviderUserId;
    }
}
