package com.zik.faro.api.unit;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.zik.faro.api.authentication.FaroJwtTokenManager;
import com.zik.faro.api.authentication.JwtClaimConstants;
import com.zik.faro.api.authentication.JwtTokenValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by granganathan on 3/7/15.
 */
public class FaroJwtTokenManagerTest {
    private static final String JWT_SIGNATURE_SECRET = "SQAAGREEnsYCx8LXBXyBn9zfzHYZxa0TC4CmJRyZ";

    @Test
    public void createAndValidateTokenTest() throws Exception {
        String username = "gaurav@gmail.com";
        String token = FaroJwtTokenManager.createToken(username);
        Assert.assertNotNull(token);

        System.out.println("Token : " + token);

        FaroJwtTokenManager.FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(token);
        Assert.assertNotNull(jwtClaims);

        Assert.assertEquals(jwtClaims.getUsername(), username);
        Assert.assertEquals(jwtClaims.getEmail(), username);

        Assert.assertTrue(System.currentTimeMillis() > jwtClaims.getIssuedAtTimeInMilliSecs());

        System.out.println("JwtClaims: " + jwtClaims);
    }

    @Test(expected = JwtTokenValidationException.class)
    public void invalidJsonTokenTest() throws Exception {
        String randomJwtToken = new StringBuilder()
                .append(Base64.encodeBase64String(UUID.randomUUID().toString().getBytes()))
                .append(".")
                .append(Base64.encodeBase64String(UUID.randomUUID().toString().getBytes()))
                .append(".")
                .append(Base64.encodeBase64String(UUID.randomUUID().toString().getBytes()))
                .toString();

        FaroJwtTokenManager.FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(randomJwtToken);
    }

    @Test(expected = SignatureException.class)
    public void invalidSignatureTokenTest() throws Exception {
        String token = FaroJwtTokenManager.createToken("gr174@gmail.com");
        String[] tokenSegments = token.split("\\.");

        System.out.println("token : " + token);

        String randomSignature = Base64.encodeBase64String(UUID.randomUUID().toString().getBytes());
        String invalidSignatureToken = new StringBuilder().append(tokenSegments[0])
                .append(".")
                .append(tokenSegments[1])
                .append(".")
                .append(randomSignature)
                .toString();

        System.out.println("invalidSignatureToken : " + invalidSignatureToken);

        FaroJwtTokenManager.FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(invalidSignatureToken);
    }
}
