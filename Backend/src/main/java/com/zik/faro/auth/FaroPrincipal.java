package com.zik.faro.auth;

import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.JwtClaims;

import java.security.Principal;

/**
 * Created by granganathan on 3/30/15.
 */
public class FaroPrincipal implements Principal {
    private FaroJwtClaims jwtClaims;

    public FaroPrincipal(FaroJwtClaims jwtClaims) {
        this.jwtClaims = jwtClaims;
    }

    public JwtClaims getJwtClaims() {
        return jwtClaims;
    }

    @Override
    public String getName() {
        return jwtClaims.getUsername();
    }
}
