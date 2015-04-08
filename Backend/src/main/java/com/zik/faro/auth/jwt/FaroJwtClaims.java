package com.zik.faro.auth.jwt;

/**
 * Created by granganathan on 3/30/15.
 */

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.security.Principal;

/**
 * Class for specifying the claims for creating a token
 * or for retrieving the claim from a token
 */
public class FaroJwtClaims implements JwtClaims, Principal {
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

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public long getIssuedAtInMilliSecs() {
        return issuedAt;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
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

    public void setExpirationInSecs(long expiration) {
        this.expiration = expiration;
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
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
