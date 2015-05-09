package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.data.user.FaroUser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by granganathan on 3/2/15.
 */
@Ignore
public class FunctionalLoginHandlerTest {
    private static URL endpoint;

    public FaroSignupDetails createNewTestUser() throws IOException, URISyntaxException {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                                        "sachin",
                                        "ramesh",
                                        "tendulkar",
                                        "splitwise",
                                        null,
                                        null);
        FaroSignupDetails faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        TestHelper.signupUser(faroSignupDetails);

        return faroSignupDetails;
    }



    @Test
    public void loginTest() throws Exception {
        // Create a user first
        FaroSignupDetails userSignupDetails = createNewTestUser();

        ClientResponse response = TestHelper.login(userSignupDetails.getFaroUser().getId(), userSignupDetails.getPassword());

        System.out.println("response = " + response);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        String token = response.getEntity(String.class);
        Assert.assertNotNull(token);
        System.out.println("token = " + token);
    }
}
