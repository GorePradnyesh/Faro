package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by granganathan on 3/2/15.
 */
@Ignore
public class FunctionalLoginHandlerTest {
    private static URL endpoint;

    @BeforeClass
    public static void init() throws MalformedURLException {
        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void loginTest() throws Exception {
        Client client = Client.create();
        WebResource webResource = client.resource(endpoint.toURI());
        String username = "gaurav@gmail.com";
        String password = "saveTheSpeedsterSaveTheWorld2032!&";
        String body = TestHelper.getJsonRep(password);

        ClientResponse response = webResource
                .path("v1")
                .path("nativeLogin")
                .path("login")
                .queryParam("username", username)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, body);

        System.out.println("response = " + response);

    }
}
