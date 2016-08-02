package com.zik.faro.functional;

import com.sun.jersey.api.client.ClientResponse;
import com.zik.faro.TestHelper;
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.UUID;

/**
 * Created by granganathan on 2/22/15.
 */
//@Ignore
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
        String token = response.getEntity(String.class);
        Assert.assertNotNull(token);
        System.out.println("token: " + token);

        // create another signup request for the same user
        ClientResponse secondResponse = TestHelper.signupUser(faroSignupDetails);
        System.out.println("response2 = " + secondResponse);
        Assert.assertEquals(secondResponse.getStatus(), ClientResponse.Status.CONFLICT.getStatusCode());
    }

}
