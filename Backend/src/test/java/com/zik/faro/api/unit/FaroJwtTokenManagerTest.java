package com.zik.faro.api.unit;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.auth.jwt.JwtTokenValidationException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.text.MessageFormat;
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
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNlYzQ2NWJkMTNhN2RhY2RmNTQ1ODk0OTJiMTdjZDNmNDFiNWNlNmYifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFyby01NjA0MyIsIm5hbWUiOiJHYXVyYXYgUmFuZ2FuYXRoYW4iLCJwaWN0dXJlIjoiaHR0cHM6Ly9zY29udGVudC54eC5mYmNkbi5uZXQvdi90MS4wLTEvcDEwMHgxMDAvMTI5MjMxMjFfMTAxNTY3MTQ5NzEyNDAwMDZfMjA4MjgwNzM2MjMxMzI3OTcxX24uanBnP29oPTg0ZTFmODQ1OTA0MDA2YTIwZDQwNTljNzY5YzNiOWQ4Jm9lPTU5QzFGRjQ1IiwiYXVkIjoiZmFyby01NjA0MyIsImF1dGhfdGltZSI6MTQ5NTMyNTcyMywidXNlcl9pZCI6IjBFU0ZtNUlyVkdNcVl4NDVUSllRM3BzQWdRZzEiLCJzdWIiOiIwRVNGbTVJclZHTXFZeDQ1VEpZUTNwc0FnUWcxIiwiaWF0IjoxNDk1MzI1NzI0LCJleHAiOjE0OTUzMjkzMjQsImVtYWlsIjoiZ2F1cmF2LnJhbmdhbmF0aGFuQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJmYWNlYm9vay5jb20iOlsiMTAxNTc0NDE4OTk1NDAwMDYiXSwiZW1haWwiOlsiZ2F1cmF2LnJhbmdhbmF0aGFuQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6ImZhY2Vib29rLmNvbSJ9fQ.A3D2IeXk4b6Ipt6yw-BZgnQrRsuU_PqV0XXSF0A4oOBubgcWvp1uoVGSWdl0RjLkXBed36q9TrdifQHiWU06kaibHaSfSXGfwCVsD_ai8C1wlfWIZ67d2Wbr1bAZiEDNequZAiQjUIsmE-p9dnd_6_NxXJXf39lL5mxlwIHEcoIaA0IQdKLp2Nc3KKhpZI3kr3OkvKY7HeXioWjf69GmQu38W0Nkov0GsyEL-BkAZKFf0bysysOVtXcVH8aU-RtMqNudR-tB67YyHB67Oyk6rIkSFewE2h3sZ-WlRkYyB3qF9DkSTNRbgMnnN1OATIgw9Prm-pTwDOEyaRKEufm4Eg\n";
        // Initialize the Firebase Admin SDK
        FileInputStream serviceAccount = null;

        // use the path to serviceAccountKey.json
        serviceAccount = new FileInputStream("/Users/granganathan/Projects/Faro/Backend/faro-56043-firebase-adminsdk-out3y-bdb49f0641.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://faro-56043.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

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

        logger.info(MessageFormat.format("firebaseToken uid = {0}, email = {1}, issuer = {2}, claims = {3}, name = {4}",
                firebaseToken.getUid(), firebaseToken.getEmail(), firebaseToken.getIssuer(),
                firebaseToken.getClaims(), firebaseToken.getName()));

        Gson gson = new Gson();
        String firebaseClaimJson = firebaseToken.getClaims().get("firebase").toString();
        logger.info(firebaseClaimJson);

        assertThat(firebaseToken).isNotNull();
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
