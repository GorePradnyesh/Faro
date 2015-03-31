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
        ClientResponse response = TestHelper.signupUser(faroSignupDetails);

        System.out.println("response = " + response);
        Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());

        // create another signup request for the same user
        ClientResponse secondResponse = TestHelper.signupUser(faroSignupDetails);
        System.out.println("response2 = " + secondResponse);
        Assert.assertEquals(secondResponse.getStatus(), ClientResponse.Status.CONFLICT.getStatusCode());
    }

}
