package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.FaroSignupDetails;
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

//@Ignore
public class FunctionalPasswordHandlerTest {
    private static URL endpoint;
    private static FaroSignupDetails faroSignupDetails;

    @BeforeClass
    public static void createNewTestUser() throws IOException, URISyntaxException {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);
        faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        TestHelper.signupUser(faroSignupDetails);
    }

    @Test
    public void resetPasswordTest() throws Exception {
        ClientResponse response = TestHelper.login(faroSignupDetails.getFaroUser().getId(), faroSignupDetails.getPassword());

        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        String token = response.getEntity(String.class);
        Assert.assertNotNull(token);
        System.out.println("token = " + token);

        Client c = Client.create();
        WebResource webResource = c.resource(endpoint.toURI());
        webResource.path("v1")
                .path("nativeLogin")
                .path("password")
                .path("resetPassword")
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .header("authentication", token)
                .put();
    }

    @Test
    public void forgotPasswordTest() throws Exception {
        Client c = Client.create();
        WebResource webResource = c.resource(TestHelper.getExternalTargetEndpoint().toURI());

        // Send forgotPassword request
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
        String htmlResponse = webResource.path(forgotPasswordFormApiPath)
                .queryParam("token", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        Assert.assertNotNull(htmlResponse);
        System.out.println(htmlResponse);

        // Send newPassword request
        System.out.println("Sending new password request ......");
        ClientResponse response = webResource.path("v1/nativeLogin/password/newPassword")
                .header("Authentication", token)
                .put(ClientResponse.class);
        Assert.assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());

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
