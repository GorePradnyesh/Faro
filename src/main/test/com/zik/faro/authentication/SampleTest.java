package com.zik.faro.authentication;


import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nakulshah on 11/30/14.
 */
public class SampleTest {

    @Test
    public void tokenGenTest() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        String sampleUID = UUID.randomUUID().toString();

        /*String secretString  = UUID.randomUUID().toString();*/
        String secret = "SQAAGREEnsYCx8LXBXyBn9zfzHYZxa0TC4CmJRyZ";
        String base64EncodedSecret  = Base64.encodeBase64String(secret.getBytes("UTF-8"));

        long iat = (new Date()).getTime();

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("uid", sampleUID);
        claims.put("iat", iat);

        JWTSigner signer = new JWTSigner(secret);
        String token = signer.sign(claims, null);

        JWTVerifier verifier = new JWTVerifier(base64EncodedSecret);
        Map<String, Object> decodedClaims = verifier.verify(token);

        Assert.assertEquals(claims, decodedClaims);


    }
}
