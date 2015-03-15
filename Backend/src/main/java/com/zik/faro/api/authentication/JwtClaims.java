package com.zik.faro.api.authentication;

/**
 * Created by granganathan on 3/8/15.
 */
public interface JwtClaims {
    public String getIssuer();
    public long getIssuedAtTimeInMilliSecs();
    public String getUsername();
    public String getEmail();
    public long getExpirationTimeInMilliSecs();
}