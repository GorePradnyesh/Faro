package com.zik.faro.auth.jwt;

/**
 * Created by granganathan on 3/8/15.
 */
public interface JwtClaims {
    public String getIssuer();
    public long getIssuedAtInMilliSecs();
    public long getExpirationInSecs();
    public String getUsername();
    public String getEmail();
}