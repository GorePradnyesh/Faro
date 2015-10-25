package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.RestClient;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.data.user.FaroUser;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by granganathan on 3/24/15.
 */
//@Ignore
public class FunctionalTestApiTest {
    private static URL endpoint;

    @BeforeClass
    public static void init() throws MalformedURLException {
        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void testApi() throws Exception {
        Client client = RestClient.getInstance().getClient();;
        WebResource webResource = client.resource(endpoint.toURI());

        String token = FaroJwtTokenManager.createToken(new FaroJwtClaims("faro", System.currentTimeMillis(), "randomUser@yahoo.com", "randomUser@yahoo.com"));
        webResource.header("authentication", token);

        ClientResponse response = webResource
                .path("v1")
                .path("test")
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .header("authentication", token)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
    }
}
