package com.zik.faro.api.unit;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.security.SignatureException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by granganathan on 3/7/15.
 */
public class FaroJwtTokenManagerTest {

    @Test
    public void createAndValidateTokenTest() throws Exception {
        String username = "gaurav@gmail.com";
        String token = FaroJwtTokenManager.createToken(username);
        Assert.assertNotNull(token);

        System.out.println("Token : " + token);

        FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(token);
        Assert.assertNotNull(jwtClaims);

        Assert.assertEquals(jwtClaims.getUsername(), username);
        Assert.assertEquals(jwtClaims.getEmail(), username);

        Assert.assertTrue(System.currentTimeMillis() > jwtClaims.getIssuedAtInMilliSecs());

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

        FaroJwtTokenManager.validateToken(randomJwtToken);
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

        FaroJwtClaims jwtClaims = FaroJwtTokenManager.validateToken(invalidSignatureToken);
    }

    @Test(expected = JwtTokenValidationException.class)
    public void tokenExpirationTest() throws Exception {
        FaroJwtClaims faroJwtClaims = new FaroJwtClaims();
        faroJwtClaims.setIssuer("faro");
        faroJwtClaims.setUsername("johndoe007@gmail.com");
        faroJwtClaims.setEmail("johndoe007@gmail.com");
        long currentTime = System.currentTimeMillis();
        faroJwtClaims.setIssuedAtInMilliSecs(currentTime);
        faroJwtClaims.setExpirationInSecs(TimeUnit.MILLISECONDS.toSeconds(currentTime) + 5);

        String token = FaroJwtTokenManager.createToken(faroJwtClaims);
        System.out.println("Created new token = " + token);

        try {
            FaroJwtTokenManager.validateToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Token validation should have succeeded");
        }
        System.out.println("Token validated.");

        Thread.sleep(TimeUnit.SECONDS.toMillis(6));
        System.out.println("Checking token validity after 6 secs");
        FaroJwtTokenManager.validateToken(token);
    }
}
