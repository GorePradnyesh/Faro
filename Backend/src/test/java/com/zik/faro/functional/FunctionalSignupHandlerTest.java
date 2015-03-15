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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by granganathan on 2/22/15.
 */
@Ignore
public class FunctionalSignupHandlerTest {
    private static URL endpoint;

    @BeforeClass
    public static void init() throws MalformedURLException {
        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void signupTest() throws Exception {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                                        "sachin",
                                        "ramesh",
                                        "tendulkar",
                                        "splitwise",
                                        null,
                                        null);
        FaroSignupDetails faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        createLoginRequest(faroSignupDetails);
    }

    public void createLoginRequest(FaroSignupDetails newUserSignupDetails) throws URISyntaxException, IOException {
        Client client = Client.create();
        WebResource webResource = client.resource(endpoint.toURI());
        String body = TestHelper.getJsonRep(newUserSignupDetails);
        System.out.println("body = " +body);

        ClientResponse response = webResource
                .path("v1")
                .path("nativeLogin")
                .path("signup")
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, body);

        System.out.println("response = " + response);

        Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
    }
}
