package com.zik.faro.api.unit;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ArrayMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.zik.faro.TestHelper;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by granganathan on 3/7/15.
 */
public class FaroJwtTokenManagerTest {
    private final Logger logger = Logger.getLogger(FaroJwtTokenManager.class);

    @Test
    public void firebaseAuthVerifyTokenTest() throws IOException {
        // Example token obtained from firebase authentication on android
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImViMmE0MTc4YWJmZmQwOThhMzhhNGFmOWFlNWYwYWEzMDY3MjhhY2EifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFyby01NjA0MyIsIm5hbWUiOiJKYXdhaGFyIFZ5YXMiLCJwaWN0dXJlIjoiaHR0cHM6Ly9zY29udGVudC54eC5mYmNkbi5uZXQvdi90MS4wLTEvczEwMHgxMDAvMTAzNTQ2ODZfMTAxNTAwMDQ1NTI4MDE4NTZfMjIwMzY3NTAxMTA2MTUzNDU1X24uanBnP29oPTFmNGM4ODYxNTYzYmY2ZDk2NTc3NDUyZjQyNjQyNGEzJm9lPTU5QjRGNDczIiwiYXVkIjoiZmFyby01NjA0MyIsImF1dGhfdGltZSI6MTQ5NjUzMjAyNiwidXNlcl9pZCI6IjR4cUwwcld5dUlmWGRXWDBSTDhFSUZ1dzJrNzMiLCJzdWIiOiI0eHFMMHJXeXVJZlhkV1gwUkw4RUlGdXcyazczIiwiaWF0IjoxNDk2NTMyMDI3LCJleHAiOjE0OTY1MzU2MjcsImVtYWlsIjoiamF3YWhhcl96enJiZmRtX3Z5YXNAdGZibncubmV0IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImZhY2Vib29rLmNvbSI6WyIxMDUzMzI2MjAwNTQ1NDUiXSwiZW1haWwiOlsiamF3YWhhcl96enJiZmRtX3Z5YXNAdGZibncubmV0Il19LCJzaWduX2luX3Byb3ZpZGVyIjoiZmFjZWJvb2suY29tIn19.t8jy6E3lTJYNBZ53fpv0NclQ2JS_-bGP5gqOSpPSrqMXIVcHlwK2iMbJ8DZ8qOH_5GTvZWy_Td3_2aUiaaVaT5G6A-R8dwfJ_m1nBQ7eu8j4kjHVtrQFxNKEzXlNmNn49Vn3tvrthaRcu4D1pegP6OKGzI4w3X-Wo9oP4n4Gz2t6n7_RJ8-_Tj8z_9zzI7XtqIIZyCUjBIQh-BnO5Ewj9njsu0GEXisMGSxZCghySQ8bRYad2ioWjcd0Td3RF6PACHFqZwx51ymlIfcNrfw0VoF0w7THkW1q-SLdiF-cqAyQhFsmOmZIAaK4ufsziINei3xJCn7vLFq54YjqkTXTgw";

        // Init firebase
        TestHelper.initializeFirebaseAdminSdk();

        // Verify the token
        Task<FirebaseToken> task = FirebaseAuth.getInstance().verifyIdToken(token)
                .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
                    @Override
                    public void onSuccess(FirebaseToken decodedFirebaseToken) {
                        logger.info("successfully decoded and verified firebase token");
                    }
                });


        try {
            Tasks.await(task);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(task.isComplete()).isTrue();
        assertThat(task.isSuccessful()).isTrue();

        FirebaseToken firebaseToken = task.getResult();
        assertThat(firebaseToken).isNotNull();

        logger.info(MessageFormat.format("firebaseToken uid = {0}, email = {1}, issuer = {2}, claims = {3}, name = {4}",
                firebaseToken.getUid(), firebaseToken.getEmail(), firebaseToken.getIssuer(),
                firebaseToken.getClaims(), firebaseToken.getName()));

        String signInProvider = (String) ((ArrayMap<String, Object>) firebaseToken.getClaims().get("firebase")).get("sign_in_provider");
        assertThat(signInProvider).isEqualTo("facebook.com");
        logger.info(signInProvider);
    }


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
