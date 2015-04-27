package com.zik.faro.frontend.faroservice.auth;

import com.google.gson.Gson;

import java.security.Principal;

public class FaroJwtClaims implements Principal {
    private String issuer;
    private long issuedAt;
    private long expiration;
    private String username;
    private String email;

    public FaroJwtClaims(String issuer, long issuedAt, String username, String email) {
        this.issuer = issuer;
        this.issuedAt = issuedAt;
        this.username = username;
        this.email = email;
    }

    public FaroJwtClaims() {}

    public String getIssuer() {
        return issuer;
    }

    public long getIssuedAtInMilliSecs() {
        return issuedAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getExpirationInSecs() {
        return expiration;
    }

    public FaroJwtClaims setIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public FaroJwtClaims setIssuedAtInMilliSecs(long issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    public FaroJwtClaims setExpirationInSecs(long expiration) {
        this.expiration = expiration;
        return this;
    }

    public FaroJwtClaims setUsername(String username) {
        this.username = username;
        return this;
    }

    public FaroJwtClaims setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
