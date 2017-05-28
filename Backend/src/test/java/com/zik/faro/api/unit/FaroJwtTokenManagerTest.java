package com.zik.faro.api.unit;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ArrayMap;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.Map;
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
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlYjY1M2NkNWVmYzFhZjE4MjkwM2ZmMTYzYjg3OTY4OTkxNWMyYWQifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZmFyby01NjA0MyIsIm5hbWUiOiJOYWt1bCBTaGFoIiwicGljdHVyZSI6Imh0dHBzOi8vc2NvbnRlbnQueHguZmJjZG4ubmV0L3YvdDEuMC0xL3MxMDB4MTAwLzEzNzk4NDFfMTAxNTAwMDQ1NTI4MDE5MDFfNDY5MjA5NDk2ODk1MjIxNzU3X24uanBnP29oPWEwODJlMGYwMmFmY2E1ZjAzYWI1NzBjNzE3MzJiODcwJm9lPTU5QjMwMTk3IiwiYXVkIjoiZmFyby01NjA0MyIsImF1dGhfdGltZSI6MTQ5NTkzMTA4NSwidXNlcl9pZCI6ImRWbWtiYXVSOXhhblNuRFFQYkpTMG1xSUJYQzIiLCJzdWIiOiJkVm1rYmF1Ujl4YW5TbkRRUGJKUzBtcUlCWEMyIiwiaWF0IjoxNDk1OTMxMDg2LCJleHAiOjE0OTU5MzQ2ODYsImVtYWlsIjoibmFrdWxfeGtqamtsbV9zaGFoQHRmYm53Lm5ldCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJmYWNlYm9vay5jb20iOlsiMTAyODU1NzMwMzAzNTE3Il0sImVtYWlsIjpbIm5ha3VsX3hramprbG1fc2hhaEB0ZmJudy5uZXQiXX0sInNpZ25faW5fcHJvdmlkZXIiOiJmYWNlYm9vay5jb20ifX0.x-BCZyO6DGvILYE9g16jrrgTi6OIQsdpRbQPFwOhEkYhh6tTOcQTwuf47IKbwaDlxKo8k2ThvtLGd45U6er8-2HmUkv-5CySIghs63jQtA4hLEo9yB55ETrx5-CXbllAqx5MZ-xQ3r_-VGmkRVILxANp8dFF1znHpqc-xdhLzunylcBmzrdfPGEWto8MPXRLt9_b6nqDS_p27wujmC8Gaju01Yi8myr80N3BedJrWh_lY8dq-2nzEqGEmoFxSdj54SEdwiYN0amr6QvWxGrbASAzxe-rgQY19O8AZhtwi8Q63lpECc8x2d4_Joe8uWp4vpLBZZgZzLkgxxE6wT1bsA";
        // Initialize the Firebase Admin SDK
        FileInputStream serviceAccount = null;

        // use the path to serviceAccountKey json
        serviceAccount = new FileInputStream(new File("faro-56043-firebase-adminsdk-out3y-192c0b32ad.json"));
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
