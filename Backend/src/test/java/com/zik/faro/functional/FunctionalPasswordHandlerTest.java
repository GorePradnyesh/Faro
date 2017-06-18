package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.data.user.FaroResetPasswordData;
import com.zik.faro.data.user.FaroUser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by granganathan on 4/18/15.
 */

@Ignore
public class FunctionalPasswordHandlerTest {
    private static URL endpoint;
    private static FaroSignupDetails faroSignupDetails;
    private static String password = "hero123#";

    @BeforeClass
    public static void initTests() throws IOException, URISyntaxException {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);
        faroSignupDetails = new FaroSignupDetails(newUser, password);
        TestHelper.signupUser(faroSignupDetails);

        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void resetPasswordTest() throws Exception {
        // Login
        ClientResponse response = TestHelper.login(faroSignupDetails.getFaroUser().getId(), faroSignupDetails.getPassword());
        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        String token = response.getEntity(String.class);
        Assert.assertNotNull(token);
        System.out.println("token = " + token);

        // Reset the password
        String newPassword = "newheroes298";
        Client client = Client.create();
        WebResource webResource = client.resource(endpoint.toURI());
        ClientResponse clientResponse = webResource.path("v1")
                .path("nativeLogin")
                .path("password")
                .path("resetPassword")
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .header("authentication", token)
                .put(ClientResponse.class, new FaroResetPasswordData(password, newPassword));

        Assert.assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), clientResponse.getStatus());

        // Verify login fails with old password and succeeds with new password
        verifyPasswordChange(faroSignupDetails.getFaroUser().getId(), password, newPassword);
    }

    @Test
    public void forgotPasswordFlowTest() throws Exception {
        Client c = Client.create();
        WebResource webResource = c.resource(TestHelper.getExternalTargetEndpoint().toURI());

        // Send forgotPassword request
        System.out.println("Sending forgot password request");
        String forgotPasswordFormUrl = webResource.path("v1")
                                    .path("nativeLogin")
                                    .path("password")
                                    .path("forgotPassword")
                                    .queryParam("username", faroSignupDetails.getFaroUser().getId())
                                    .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                                    .get(String.class);

        Assert.assertNotNull(forgotPasswordFormUrl);
        System.out.println("forgotPasswordFormUrl = " + forgotPasswordFormUrl);

        String forgotPasswordFormApiPath = getApiPathFromUrl(forgotPasswordFormUrl);
        String token = getQueryParamFromUrl(forgotPasswordFormUrl, "token");
        System.out.println("token from url = " + token);
        // Send forgotPasswordForm request
        System.out.println("Sending forgot password form request");
        String htmlResponse = webResource.path(forgotPasswordFormApiPath)
                .queryParam("token", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        Assert.assertNotNull(htmlResponse);
        System.out.println(htmlResponse);

        // Send newPassword request
        String newPassword = "mynewpassword67#";
        System.out.println("Sending new password request");
        ClientResponse response = webResource.path("v1/nativeLogin/password/newPassword")
                .header("Authentication", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, newPassword);
        Assert.assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        // Verify login fails with old password and succeeds with new password
        verifyPasswordChange(faroSignupDetails.getFaroUser().getId(), password, newPassword);
    }

    private void verifyPasswordChange(String userId, String oldPassword, String newPassword) throws Exception {
        // Login should fail with the old password
        ClientResponse response = TestHelper.login(userId, oldPassword);
        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

        // Login should succeed with the new password
        response = TestHelper.login(userId, newPassword);
        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        String token = response.getEntity(String.class);
        Assert.assertNotNull(token);
        System.out.println("token = " + token);
    }

    private String getApiPathFromUrl(String url) {
        if(!url.contains("?")) {
            return url.substring(url.indexOf("v1"), url.length() - 1);
        } else {
            return url.substring(url.indexOf("v1"), url.indexOf("?"));
        }
    }

    private String getQueryParamFromUrl(String url, String queryParamName) {
        int queryParamNameIndex = url.indexOf(queryParamName + "=") + queryParamName.length() + 1;

        return url.substring(queryParamNameIndex, url.length());
    }
}
